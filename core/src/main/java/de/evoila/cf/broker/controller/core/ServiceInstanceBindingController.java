package de.evoila.cf.broker.controller.core;

import de.evoila.cf.broker.controller.BaseController;
import de.evoila.cf.broker.exception.*;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.model.annotations.ApiVersion;
import de.evoila.cf.broker.service.CatalogService;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**  @author Marco Di Martino, Johannes Hiemer */
@Controller
@RequestMapping(value = "/v2/service_instances")
public class ServiceInstanceBindingController extends BaseController {

	private final Logger log = LoggerFactory.getLogger(ServiceInstanceBindingController.class);

	public static final String SERVICE_INSTANCE_BINDING_BASE_PATH = "/core/service_instances/{instanceId}/service_bindings";

	private BindingServiceImpl bindingService;

	private CatalogService catalogService;

	public ServiceInstanceBindingController(BindingServiceImpl bindingService, CatalogService catalogService) {
		this.bindingService = bindingService;
		this.catalogService = catalogService;
	}

	@PutMapping(value = "/{instanceId}/service_bindings/{bindingId}")
	@ApiVersion({ApiVersions.API_213, ApiVersions.API_214})
	public ResponseEntity<ServiceInstanceBindingResponse> bindServiceInstance(@PathVariable("instanceId") String instanceId,
            @PathVariable("bindingId") String bindingId,
			@RequestHeader("X-Broker-API-Version") String apiHeader,
			@RequestParam(value = "accepts_incomplete", required = false, defaultValue = "") Boolean acceptsIncomplete,
			@Valid @RequestBody ServiceInstanceBindingRequest request)
					throws ServiceInstanceDoesNotExistException, ServiceInstanceBindingExistsException,
					ServiceBrokerException, ServiceDefinitionDoesNotExistException,
					ServiceBrokerFeatureIsNotSupportedException, PlatformException,
					InvalidParametersException, AsyncRequiredException, UnsupportedOperationException {

		log.debug("PUT: " + SERVICE_INSTANCE_BINDING_BASE_PATH + "/{bindingId}"
				+ ", bindServiceInstance(), instanceId = " + instanceId + ", bindingId = " + bindingId);

		// AppGuid Field is deprecated and won't be maintained in future. According to OSB spec at:
        // https://github.com/openservicebrokerapi/servicebroker/blob/master/spec.md#bind-resource-object
        // AppGuid may not be present and empty
		if (request.getAppGuid() != null && request.getAppGuid().isEmpty())
            return new ResponseEntity("{}", HttpStatus.BAD_REQUEST);

		if (acceptsIncomplete == null) {
			acceptsIncomplete = false;
		}
		if (acceptsIncomplete && apiHeader.equals("2.13")) {
			throw new ServiceInstanceBindingBadRequestException(bindingId);
		}

		ServiceInstanceBindingResponse response = bindingService.createServiceInstanceBinding(bindingId, instanceId, request, acceptsIncomplete);

		log.debug("ServiceInstanceBinding Created: " + bindingId);

		if (acceptsIncomplete)
			return new ResponseEntity<>(new ServiceInstanceBindingResponse(true), HttpStatus.ACCEPTED);
		else
			return new ResponseEntity<>(response, HttpStatus.CREATED);

	}

	@DeleteMapping(value = "/{instanceId}/service_bindings/{bindingId}")
	@ApiVersion({ApiVersions.API_213, ApiVersions.API_214})
	public ResponseEntity<String> deleteServiceInstanceBinding(@PathVariable("instanceId") String instanceId,
			@PathVariable("bindingId") String bindingId, @RequestParam("service_id") String serviceId,
			@RequestParam("plan_id") String planId, @RequestParam(value = "accepts_incomplete", required = false, defaultValue = "") Boolean acceptsIncomplete,
			@RequestHeader("X-Broker-API-Version") String apiHeader) throws ServiceBrokerException, AsyncRequiredException{

		log.debug("DELETE: " + SERVICE_INSTANCE_BINDING_BASE_PATH + "/{bindingId}"
				+ ", deleteServiceInstanceBinding(),  serviceInstance.id = " + instanceId + ", bindingId = " + bindingId
				+ ", serviceId = " + serviceId + ", planId = " + planId);

		if (acceptsIncomplete == null) {
			acceptsIncomplete = false;
		}
		if (acceptsIncomplete && apiHeader.equals("2.13")) {
			throw new ServiceInstanceBindingBadRequestException(bindingId);
		}
		try {
			bindingService.deleteServiceInstanceBinding(bindingId, planId, acceptsIncomplete);
		} catch(ServiceInstanceBindingDoesNotExistsException | ServiceDefinitionDoesNotExistException e) {
			return new ResponseEntity<>("{}", HttpStatus.GONE);
		}

		log.debug("ServiceInstanceBinding Deleted: " + bindingId);

		if (acceptsIncomplete) {
			return new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("{}", HttpStatus.OK);
		}
	}

	@GetMapping(value = "/{instanceId}/service_bindings/{bindingId}/last_operation")
	@ApiVersion(ApiVersions.API_214)
	public ResponseEntity<JobProgressResponse> lastOperation(@PathVariable("instanceId") String instanceId,
																		@PathVariable("bindingId") String bindingId,
																		@RequestParam(value = "service_id", required = false) String serivceId,
																		@RequestParam(value = "plan_id", required = false) String planId,
																		@RequestHeader("X-Broker-API-Version") String apiHeader)
			throws ServiceInstanceBindingDoesNotExistsException {

		JobProgressResponse bindingProcessingResponse = bindingService.getLastOperation(bindingId);

		return new ResponseEntity<>(bindingProcessingResponse, HttpStatus.OK);
	}

	@GetMapping(value = "/{instanceId}/service_bindings/{bindingId}")
	@ApiVersion(ApiVersions.API_214)
	public ResponseEntity<ServiceInstanceBindingResponse> fetchServiceInstanceBinding(@PathVariable("instanceId") String instanceId,
																					  @PathVariable("bindingId") String bindingId
																					  ) throws ServiceInstanceBindingNotFoundException, ServiceBrokerException, ServiceInstanceDoesNotExistException{
		ServiceInstance serviceInstance = bindingService.getServiceInstance(instanceId);
		if (!(catalogService.getServiceDefinition(serviceInstance.getServiceDefinitionId()).isBindingsRetrievable())) {
			throw new ServiceInstanceBindingNotRetrievableException("The Service Binding could not be retrievable. You should not attempt to call this endpoint");
		}

		ServiceInstanceBinding binding =  bindingService.fetchServiceInstanceBinding(bindingId, instanceId);
		ServiceInstanceBindingResponse response = new ServiceInstanceBindingResponse(binding);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(ServiceInstanceDoesNotExistException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(ServiceInstanceDoesNotExistException ex) {
		return processErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@ExceptionHandler(ServiceBrokerFeatureIsNotSupportedException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(ServiceBrokerFeatureIsNotSupportedException ex) {
		return processErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(ServiceInstanceBindingExistsException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(ServiceInstanceBindingExistsException ex) {
		return processErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler({ServiceInstanceBindingBadRequestException.class, ServiceInstanceBindingNotRetrievableException.class, InvalidParametersException.class})
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(Exception ex) {
		return processErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ServiceInstanceBindingNotFoundException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(ServiceInstanceBindingNotFoundException ex) {
		return processErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ServiceInstanceBindingDoesNotExistsException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(ServiceInstanceBindingDoesNotExistsException ex) {
		return processErrorResponse(ex.getMessage(), HttpStatus.GONE);
	}
}

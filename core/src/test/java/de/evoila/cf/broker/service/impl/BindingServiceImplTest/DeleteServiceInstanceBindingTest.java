package de.evoila.cf.broker.service.impl.BindingServiceImplTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.evoila.cf.broker.exception.AsyncRequiredException;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.exception.ServiceDefinitionDoesNotExistException;
import de.evoila.cf.broker.exception.ServiceInstanceBindingDoesNotExistsException;
import de.evoila.cf.broker.model.ServiceInstanceBindingOperationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteServiceInstanceBindingTest extends BaseTest {

    @Test
    void getServiceInstanceByBindingIdThrows() throws ServiceInstanceBindingDoesNotExistsException {
        ServiceInstanceBindingDoesNotExistsException expectedEx = new ServiceInstanceBindingDoesNotExistsException("Mock");
        doThrow(expectedEx)
                .when(service)
                .getServiceInstanceByBindingId(HAPPY_BINDING_ID);
        ServiceInstanceBindingDoesNotExistsException ex = assertThrows(ServiceInstanceBindingDoesNotExistsException.class,
                                                                       () -> service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                                                  HAPPY_PLAN_ID,
                                                                                                                  HAPPY_ASYNC));
        assertSame(expectedEx, ex);
    }

    @Nested
    class getServiceInstanceByBindingIdDoesNotThrown {

        @BeforeEach
        void setUp() throws ServiceInstanceBindingDoesNotExistsException {
            doReturn(serviceInstance)
                    .when(service)
                    .getServiceInstanceByBindingId(HAPPY_BINDING_ID);
        }

        @Test
        void getPlanThrows() throws ServiceDefinitionDoesNotExistException {
            ServiceDefinitionDoesNotExistException expectedEx = new ServiceDefinitionDoesNotExistException("Mock");
            when(serviceDefinitionRepository.getPlan(HAPPY_PLAN_ID))
                    .thenThrow(expectedEx);
            ServiceDefinitionDoesNotExistException ex = assertThrows(ServiceDefinitionDoesNotExistException.class,
                                                                     () -> service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                                                HAPPY_PLAN_ID,
                                                                                                                HAPPY_ASYNC));
            assertSame(expectedEx, ex);
        }

        @Nested
        class getPlanDoesNotThrow {

            @BeforeEach
            void setUp() throws ServiceDefinitionDoesNotExistException {
                when(serviceDefinitionRepository.getPlan(HAPPY_PLAN_ID))
                        .thenReturn(plan);
                when(plan.getPlatform())
                        .thenReturn(HAPPY_PLATFORM);
            }

            @Test
            void getPlatformServiceReturnsNull() {
                when(platformRepository.getPlatformService(HAPPY_PLATFORM))
                        .thenReturn(null);
                ServiceBrokerException expectedEx = new ServiceBrokerException("No Platform configured for " + plan.getPlatform());
                ServiceBrokerException ex = assertThrows(ServiceBrokerException.class,
                                                         () -> service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                                    HAPPY_PLAN_ID,
                                                                                                    HAPPY_ASYNC));
                assertEquals(expectedEx, ex);
            }

            @Nested
            class getPlatformServiceDoesNotReturnNull {

                @BeforeEach
                void setUp() {
                    when(platformRepository.getPlatformService(HAPPY_PLATFORM))
                            .thenReturn(platformService);
                    when(randomString.nextString())
                            .thenReturn(HAPPY_OPERATION_ID);
                }

                @Nested
                class isSyncPossibleOnUnbindReturnsTrue {

                    @BeforeEach
                    void setUp() {
                        when(platformService.isSyncPossibleOnUnbind())
                                .thenReturn(true);
                        doNothing()
                                .when(service)
                                .syncDeleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                  serviceInstance,
                                                                  plan);
                    }

                    private void validateResponse(ServiceInstanceBindingOperationResponse response) {
                        ServiceInstanceBindingOperationResponse expectedResponse = new ServiceInstanceBindingOperationResponse(HAPPY_OPERATION_ID,
                                                                                                                               false);
                        verify(service, times(1))
                                .syncDeleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                  serviceInstance,
                                                                  plan);
                        assertEquals(expectedResponse, response);
                    }

                    @Test
                    void asnyIsTrue() throws ServiceDefinitionDoesNotExistException, AsyncRequiredException, ServiceBrokerException, ServiceInstanceBindingDoesNotExistsException {
                        ServiceInstanceBindingOperationResponse response = (ServiceInstanceBindingOperationResponse) service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                                                                                          HAPPY_PLAN_ID,
                                                                                                                                                          HAPPY_ASYNC);
                        validateResponse(response);
                    }

                    @Test
                    void asnyIsFalse() throws ServiceDefinitionDoesNotExistException, AsyncRequiredException, ServiceBrokerException, ServiceInstanceBindingDoesNotExistsException {
                        ServiceInstanceBindingOperationResponse response = (ServiceInstanceBindingOperationResponse) service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                                                                                          HAPPY_PLAN_ID,
                                                                                                                                                          false);
                        validateResponse(response);
                    }

                }

                @Nested
                class isSyncPossibleOnUnbindReturnsFalse {

                    @BeforeEach
                    void setUp() {
                        when(platformService.isSyncPossibleOnUnbind())
                                .thenReturn(false);
                    }

                    @Test
                    void asyncIsFalse() {
                        assertThrows(AsyncRequiredException.class,
                                     () -> service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                HAPPY_PLAN_ID,
                                                                                false));
                    }

                    @Test
                    void asyncIsTrue() throws ServiceDefinitionDoesNotExistException, AsyncRequiredException, ServiceBrokerException, ServiceInstanceBindingDoesNotExistsException {
                        ServiceInstanceBindingOperationResponse expectedResponse = new ServiceInstanceBindingOperationResponse(HAPPY_OPERATION_ID,
                                                                                                                               true);
                        ServiceInstanceBindingOperationResponse response = (ServiceInstanceBindingOperationResponse) service.deleteServiceInstanceBinding(HAPPY_BINDING_ID,
                                                                                                                                                          HAPPY_PLAN_ID,
                                                                                                                                                          HAPPY_ASYNC);
                        verify(asyncBindingService, times(1))
                                .asyncDeleteServiceInstanceBinding(service,
                                                                   HAPPY_BINDING_ID,
                                                                   serviceInstance,
                                                                   plan,
                                                                   HAPPY_OPERATION_ID);
                        assertEquals(expectedResponse, response);
                    }

                }

            }

        }

    }

}
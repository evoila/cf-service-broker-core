//package de.evoila.cf.broker.bean.utils;
//
//import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
//import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
//import org.springframework.context.annotation.ConditionContext;
//import org.springframework.core.env.PropertyResolver;
//import org.springframework.core.type.AnnotatedTypeMetadata;
//
//import java.util.Map;
//
//public class SiteConfigurationCondition extends SpringBootCondition {
//
//    private static String PREFIX_AND_VALUES = "site.properties.";
//
//    @Override
//    public ConditionOutcome getMatchOutcome(final ConditionContext context,
//                                            final AnnotatedTypeMetadata metadata) {
//        final PropertySourcesPropertyResolver resolver =context.getEnvironment().get
//        final Map<String, Object> properties = resolver.getSubProperties(PREFIX_AND_VALUES);
//        return new ConditionOutcome(!properties.isEmpty(), "SiteConfiguration");
//    }
//}
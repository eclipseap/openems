package io.openems.edge.goodwe.batteryinverter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.goodwe.GoodWeConstants;

@ObjectClassDefinition(//
		name = "GoodWe Battery-Inverter", //
		description = "GoodWe Battery Inverter.")

@interface Config {
	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "batteryInverter0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus bridge.")
	String modbus_id() default "modbus0";

	@AttributeDefinition(name = "Modbus Unit-ID", description = "The Unit-ID of the Modbus device.")
	int modbusUnitId() default GoodWeConstants.DEFAULT_UNIT_ID;
	
	@AttributeDefinition(name = "Battery String", description = "The total amount off battery module .")
	int batteryString() default 5;
	
	@AttributeDefinition(name = "Lead Battery Capacity", description = "Lead Battery Capacity.")
	int leadBatteryCapacity() default 200;

	@AttributeDefinition(name = "Modbus target filter", description = "This is auto-generated by 'Modbus-ID'.")
	String Modbus_target() default "";

	String webconsole_configurationFactory_nameHint() default "GoodWe Battery Inverter [{id}]";
}
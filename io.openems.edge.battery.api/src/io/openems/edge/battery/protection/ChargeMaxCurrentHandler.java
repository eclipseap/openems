package io.openems.edge.battery.protection;

import io.openems.edge.battery.protection.force.ForceDischarge;
import io.openems.edge.common.component.ClockProvider;
import io.openems.edge.common.linecharacteristic.PolyLine;

public class ChargeMaxCurrentHandler extends AbstractMaxCurrentHandler {

	public static class Builder extends AbstractMaxCurrentHandler.Builder<Builder> {

		private ForceDischarge.Params forceDischargeParams = null;

		protected Builder(ClockProvider clockProvider, int initialBmsMaxEverAllowedChargeCurrent) {
			super(clockProvider, initialBmsMaxEverAllowedChargeCurrent);
		}

		/**
		 * Configure 'Force Discharge' parameters.
		 * 
		 * @param startDischargeAboveCellVoltage start force discharge if maxCellVoltage
		 *                                       is above this value, e.g. 3660
		 * @param dischargeAboveCellVoltage      force discharge as long as
		 *                                       maxCellVoltage is above this value,
		 *                                       e.g. 3640
		 * @param blockChargeAboveCellVoltage    after 'force discharge', block charging
		 *                                       as long as maxCellVoltage is above this
		 *                                       value, e.g. 3450
		 * @return {@link Builder}
		 */
		public Builder setForceDischarge(int startDischargeAboveCellVoltage, int dischargeAboveCellVoltage,
				int blockChargeAboveCellVoltage) {
			this.forceDischargeParams = new ForceDischarge.Params(startDischargeAboveCellVoltage,
					dischargeAboveCellVoltage, blockChargeAboveCellVoltage);
			return this;
		}

		public Builder setForceDischarge(ForceDischarge.Params forceDischargeParams) {
			this.forceDischargeParams = forceDischargeParams;
			return this;
		}

		public ChargeMaxCurrentHandler build() {
			return new ChargeMaxCurrentHandler(this.clockProvider, this.initialBmsMaxEverCurrent, this.voltageToPercent,
					this.temperatureToPercent, this.maxIncreasePerSecond, this.forceDischargeParams);
		}

		@Override
		protected Builder self() {
			return this;
		}
	}

	/**
	 * Create a {@link ChargeMaxCurrentHandler} builder.
	 * 
	 * @param clockProvider                         a {@link ClockProvider}
	 * @param initialBmsMaxEverAllowedChargeCurrent the (estimated) maximum allowed
	 *                                              charge current. This is used as
	 *                                              a reference for percentage
	 *                                              values. If during runtime a
	 *                                              higher value is provided, that
	 *                                              one is taken from then on.
	 * @return a {@link Builder}
	 */
	public static Builder create(ClockProvider clockProvider, int initialBmsMaxEverAllowedChargeCurrent) {
		return new Builder(clockProvider, initialBmsMaxEverAllowedChargeCurrent);
	}

	protected ChargeMaxCurrentHandler(ClockProvider clockProvider, int initialBmsMaxEverAllowedChargeCurrent,
			PolyLine voltageToPercent, PolyLine temperatureToPercent, Double maxIncreasePerSecond,
			ForceDischarge.Params forceDischargeParams) {
		super(clockProvider, initialBmsMaxEverAllowedChargeCurrent, voltageToPercent, temperatureToPercent,
				maxIncreasePerSecond, new ForceDischarge(forceDischargeParams));
	}

}

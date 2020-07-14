package io.openems.edge.io.iot2000;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.channel.BooleanReadChannel;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.io.api.DigitalInput;
import io.openems.edge.io.api.DigitalOutput;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.io.iot2000", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
		} //
)
public class IOT2000 extends AbstractOpenemsComponent
		implements DigitalOutput, DigitalInput, OpenemsComponent, EventHandler {

	@SuppressWarnings("unused")
	private Config config = null;

	private final BooleanWriteChannel[] digitalOutputChannels;
	private final BooleanReadChannel[] digitalInputChannels;
	private final Logger log = LoggerFactory.getLogger(IOT2000.class);
	private final int[] gpioInputMap = { 15, 5, 10, 4, 6 };
	private final int[] gpioOutputMap = { 40, 38 };

	public IOT2000() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				DigitalOutput.ChannelId.values(), //
				DigitalInput.ChannelId.values(), //
				ThisChannelId.values() //
		);
		this.digitalOutputChannels = new BooleanWriteChannel[] { //
				this.channel(ThisChannelId.OUT_1), //
				this.channel(ThisChannelId.OUT_2) //

		};

		this.digitalInputChannels = new BooleanReadChannel[] { //
				this.channel(ThisChannelId.IN_1), //
				this.channel(ThisChannelId.IN_2), //
				this.channel(ThisChannelId.IN_3), //
				this.channel(ThisChannelId.IN_4), //
				this.channel(ThisChannelId.IN_5) //
		};

	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		this.deactivateAllWriteChannels();
	}

	@Deactivate
	protected void deactivate() {
		this.deactivateAllWriteChannels();
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.updateChannels();
			break;

		case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
			this.eventExecuteWrite();
			break;
		}
	}

	private void eventExecuteWrite() {
		for (int i = 0; i < this.digitalOutputChannels.length; i++) {
			BooleanWriteChannel channel = this.digitalOutputChannels[i];
			Boolean readValue = channel.value().get();
			Optional<Boolean> writeValue = channel.getNextWriteValueAndReset();

			if (!writeValue.isPresent()) {
				// no write value
				return;
			}
			if (Objects.equals(readValue, writeValue.get())) {
				// read value = write value
				return;
			}
			try {
				this.setOutputValue(this.gpioOutputMap[i], writeValue.get());

			} catch (Exception e) {
				this.logError(this.log,
						"Could not write Value to " + channel.address().toString() + " - Reason: " + e.getMessage());
				e.printStackTrace();
			}

		}

	}

	private void updateChannels() {

		this.updateIoChannels(this.digitalInputChannels, this.gpioInputMap);
		this.updateIoChannels(this.digitalOutputChannels, this.gpioOutputMap);

	}

	private void updateIoChannels(Channel[] channels, int[] gpioMap) {
		for (int i = 0; i < channels.length; i++) {
			try {
				boolean val = this.getIoValue(gpioMap[i]);

				Optional<Boolean> inOpt = Optional.ofNullable(val);

				if (!channels[i].value().asOptional().equals(inOpt)) {

					channels[i].setNextValue(val);
				}

			} catch (IOException e) {
				this.logError(this.log, "Could not read Value for channel " + channels[i].address().toString()
						+ " - Reason: " + e.getMessage());
			}
		}
	}

	@Override
	public String debugLog() {
		StringBuilder b = new StringBuilder();
		int i = 0;
		b.append("IN:");
		for (BooleanReadChannel channel : this.digitalInputChannels) {
			Optional<Boolean> valueOpt = channel.value().asOptional();
			if (valueOpt.isPresent()) {
				if (valueOpt.get()) {
					b.append("1");
				} else {
					b.append("0");
				}
			} else {
				b.append("-");
			}
			if ((i++) % 4 == 3) {
				b.append(" ");
			}
		}
		i = 0;
		b.append("  OUT:");
		for (BooleanWriteChannel channel : this.digitalOutputChannels) {
			Optional<Boolean> valueOpt = channel.value().asOptional();
			if (valueOpt.isPresent()) {
				if (valueOpt.get()) {
					b.append("1");
				} else {
					b.append("0");
				}
			} else {
				b.append("-");
			}
			if ((i++) % 4 == 3) {
				b.append(" ");
			}
		}
		return b.toString();
	}

	@Override
	public BooleanReadChannel[] digitalInputChannels() {
		return this.digitalInputChannels;
	}

	@Override
	public BooleanWriteChannel[] digitalOutputChannels() {
		return this.digitalOutputChannels;
	}

	private boolean getIoValue(int gpio) throws IOException {
		File file = new File("/sys/class/gpio/gpio" + gpio + "/value");
		FileInputStream fis = new FileInputStream(file);
		int value = fis.read();
		fis.close();

		if (value == 49) {
			return true;
		} else {
			return false;
		}

	}

	private void setOutputValue(int gpio, boolean val) throws Exception {
		File file = new File("/sys/class/gpio/gpio" + gpio + "/value");
		FileOutputStream fos = new FileOutputStream(file);
		char write = '0';
		if (val) {
			write = '1';
		}
		fos.write((byte) write);
		fos.close();
		this.logError(log, "wrote " + write + " to " + file.getAbsolutePath());
	}

	private void deactivateAllWriteChannels() {
		for (BooleanWriteChannel channel : this.digitalOutputChannels) {
			try {
				channel.setNextWriteValue(Boolean.FALSE);
			} catch (OpenemsNamedException e) {
				// ignore
			}
		}
	}

}

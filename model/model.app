<?xml version="1.0" encoding="UTF-8"?>
<app:application xmlns:app="http://www.sierrawireless.com/airvantage/application/1.0" type="makeit.avphone" name="Android Phone Monitoring" revision="0.1">

	<capabilities>
		<communication>
			<protocol comm-id="SERIAL" type="MQTT">
			</protocol>
		</communication>

		<data>
		    <encoding type="MQTT">
			<asset default-label="Android Phone" id="phone">
			    <setting default-label="RSSI" path="rssi" type="int"/>
			    <setting default-label="Service type" path="service" type="string"/>
			    <setting default-label="Operator" path="operator" type="string"/>
			    <setting default-label="Latitude" path="latitude" type="double"/>
			    <setting default-label="Longitude" path="longitude" type="double"/>
			    <setting default-label="Battery level" path="batterylevel" type="double"/>
                            <setting default-label="Bytes received" path="bytesreceived" type="double"/>
                            <setting default-label="Bytes sent" path="bytessent" type="double"/>
                            <setting default-label="Memory usage" path="memoryusage" type="double"/>
                            <setting default-label="Running applications" path="runningapps" type="int"/>
                            <setting default-label="Active Wi-Fi" path="activewifi" type="boolean"/>

                            <command default-label="Notify" path="notify" >
                               <parameter id="message" type="string" />
                            </command>
			</asset>
		    </encoding>
		</data>

	</capabilities>  

</app:application>

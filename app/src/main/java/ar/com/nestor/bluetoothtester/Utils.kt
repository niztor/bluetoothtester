package ar.com.nestor.bluetoothtester

import android.bluetooth.BluetoothClass


fun byteArrayToString(array: ByteArray?, count: Int) : String {
    return if (array == null)
        ""
    else {
        if (count <= array.size)
            String(array.copyOfRange(0, count))
        else
            ""
    }
}

fun bluetoothClassMajorToString(cls: Int) : String {
    return when (cls) {
        BluetoothClass.Device.Major.AUDIO_VIDEO -> "AudioVideo"
        BluetoothClass.Device.Major.COMPUTER -> "Computer"
        BluetoothClass.Device.Major.HEALTH -> "Health"
        BluetoothClass.Device.Major.IMAGING -> "Imaging"
        BluetoothClass.Device.Major.MISC -> "Misc"
        BluetoothClass.Device.Major.NETWORKING -> "Networking"
        BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
        BluetoothClass.Device.Major.PHONE -> "Phone"
        BluetoothClass.Device.Major.TOY -> "Toy"
        BluetoothClass.Device.Major.UNCATEGORIZED -> "Uncategorized"
        BluetoothClass.Device.Major.WEARABLE -> "Wearable"
        else -> "Unknown..."
    }

}
/*

        // Devices in the COMPUTER major class
        public static final int COMPUTER_UNCATEGORIZED = 0x0100;
        public static final int COMPUTER_DESKTOP = 0x0104;
        public static final int COMPUTER_SERVER = 0x0108;
        public static final int COMPUTER_LAPTOP = 0x010C;
        public static final int COMPUTER_HANDHELD_PC_PDA = 0x0110;
        public static final int COMPUTER_PALM_SIZE_PC_PDA = 0x0114;
        public static final int COMPUTER_WEARABLE = 0x0118;

        // Devices in the PHONE major class
        public static final int PHONE_UNCATEGORIZED = 0x0200;
        public static final int PHONE_CELLULAR = 0x0204;
        public static final int PHONE_CORDLESS = 0x0208;
        public static final int PHONE_SMART = 0x020C;
        public static final int PHONE_MODEM_OR_GATEWAY = 0x0210;
        public static final int PHONE_ISDN = 0x0214;

        // Minor classes for the AUDIO_VIDEO major class
        public static final int AUDIO_VIDEO_UNCATEGORIZED = 0x0400;
        public static final int AUDIO_VIDEO_WEARABLE_HEADSET = 0x0404;
        public static final int AUDIO_VIDEO_HANDSFREE = 0x0408;
        //public static final int AUDIO_VIDEO_RESERVED              = 0x040C;
        public static final int AUDIO_VIDEO_MICROPHONE = 0x0410;
        public static final int AUDIO_VIDEO_LOUDSPEAKER = 0x0414;
        public static final int AUDIO_VIDEO_HEADPHONES = 0x0418;
        public static final int AUDIO_VIDEO_PORTABLE_AUDIO = 0x041C;
        public static final int AUDIO_VIDEO_CAR_AUDIO = 0x0420;
        public static final int AUDIO_VIDEO_SET_TOP_BOX = 0x0424;
        public static final int AUDIO_VIDEO_HIFI_AUDIO = 0x0428;
        public static final int AUDIO_VIDEO_VCR = 0x042C;
        public static final int AUDIO_VIDEO_VIDEO_CAMERA = 0x0430;
        public static final int AUDIO_VIDEO_CAMCORDER = 0x0434;
        public static final int AUDIO_VIDEO_VIDEO_MONITOR = 0x0438;
        public static final int AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER = 0x043C;
        public static final int AUDIO_VIDEO_VIDEO_CONFERENCING = 0x0440;
        //public static final int AUDIO_VIDEO_RESERVED              = 0x0444;
        public static final int AUDIO_VIDEO_VIDEO_GAMING_TOY = 0x0448;

        // Devices in the WEARABLE major class
        public static final int WEARABLE_UNCATEGORIZED = 0x0700;
        public static final int WEARABLE_WRIST_WATCH = 0x0704;
        public static final int WEARABLE_PAGER = 0x0708;
        public static final int WEARABLE_JACKET = 0x070C;
        public static final int WEARABLE_HELMET = 0x0710;
        public static final int WEARABLE_GLASSES = 0x0714;

        // Devices in the TOY major class
        public static final int TOY_UNCATEGORIZED = 0x0800;
        public static final int TOY_ROBOT = 0x0804;
        public static final int TOY_VEHICLE = 0x0808;
        public static final int TOY_DOLL_ACTION_FIGURE = 0x080C;
        public static final int TOY_CONTROLLER = 0x0810;
        public static final int TOY_GAME = 0x0814;

        // Devices in the HEALTH major class
        public static final int HEALTH_UNCATEGORIZED = 0x0900;
        public static final int HEALTH_BLOOD_PRESSURE = 0x0904;
        public static final int HEALTH_THERMOMETER = 0x0908;
        public static final int HEALTH_WEIGHING = 0x090C;
        public static final int HEALTH_GLUCOSE = 0x0910;
        public static final int HEALTH_PULSE_OXIMETER = 0x0914;
        public static final int HEALTH_PULSE_RATE = 0x0918;
        public static final int HEALTH_DATA_DISPLAY = 0x091C;
 */
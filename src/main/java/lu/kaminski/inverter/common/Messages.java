package lu.kaminski.inverter.common;

import lombok.AllArgsConstructor;

public class Messages {

    @AllArgsConstructor
    public enum Message {
        TECHNICAL_ERROR("Technical error while %s"),
        UNKNOWN_VALUE("Unknown value [%s]"),
        DATA_ACCESS_NOT_ALLOWED("You can not access such data"),
        NO_PARAM("Parameter is mandatory for such a request"),
        NOT_IMPLEMENTED("This feature is not implemented"),

        //Data
        NO_DATA_FOUND("No data found for [%s]");

        private String message;

        public String getMessage(String... param) {
            return String.format(message, (Object[]) param);

        }
    }
}

package dto.queueManagement;

import com.compassites.model.ErrorMessage;

public class RemovePnrFromQueueRes {


    private boolean success;

    private ErrorMessage message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorMessage getMessage() {
        return message;
    }

    public void setMessage(ErrorMessage message) {
        this.message = message;
    }


}

package com.stupidbeauty.rotatingactiveuser;

public class VoiceCommandHitDataObject
{
    public void setPictureFileContent(byte[] pictureFileContent) {
        this.pictureFileContent = pictureFileContent;
    }

    byte[] pictureFileContent=null; //!<图片文件内容
    byte[] asrWavFileContent=null; //!<声音文件内容

    public void setAsrWavFileContent(byte[] asrWavFileContent) {
        this.asrWavFileContent = asrWavFileContent;
    }

    public byte[] getAsrWavFileContent() {
        return asrWavFileContent;
    }

    private String iconTitle; //!<设置图标标题．

    public void setIconTitle(String iconTitle) {
        this.iconTitle = iconTitle;
    }
    
    /**
    * 获取图标标题。陈欣
    */
    public String getIconTitle()
    {
        return iconTitle;
    } //public String getIconTitle()

    public void setVoiceRecognizeResult(String voiceRecognizeResult) {
        this.voiceRecognizeResult = voiceRecognizeResult;
    }

    public String getVoiceRecognizeResult() {
        return voiceRecognizeResult;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    private String voiceRecognizeResult;
    private String packageName;
    private String activityName;
}

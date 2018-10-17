package no.forsamling.audiobroadcast.interfaces;

public interface ITelephony {
    boolean endCall();
    void answerRingingCall();
    void silenceRinger();
}

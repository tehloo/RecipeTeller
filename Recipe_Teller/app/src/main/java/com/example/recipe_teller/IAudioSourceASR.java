package com.example.recipe_teller;

/**
 * Provides a common interface for retrieving voice data from voice sources and sending them to the
 * engine.
 */
interface IAudioSourceASR {
    /**
     * Implement that it need to prepare the voice source.
     *
     * @return true, if the source is available.
     */
    boolean prepare();

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     * Single channel
     *
     * @param buffer byte buffer to write the audio data.
     * @return the size of buffer.
     */
    int read(byte[] buffer);

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     * Multi-channel.
     *
     * @param buffers byte buffers to write the audio data.
     * @return the size of buffer.
     */
    int read(byte[][] buffers);

    /**
     * Returns channel count
     */
    int getChannel();

    /**
     * Release the resources. This is called once at the end.
     */
    void release();
}

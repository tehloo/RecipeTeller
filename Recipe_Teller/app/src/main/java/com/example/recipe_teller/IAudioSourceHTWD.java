/*
 * Copyright (c) 2018 LG Electronics Inc.
 */
package com.example.recipe_teller;

/**
 * Provides a common interface for retrieving voice data from voice sources and sending them to the
 * engine.
 */
interface IAudioSourceHTWD {
    /**
     * Implement that it need to prepare the voice source.
     *
     * @return true, if the source is available.
     */
    boolean prepare();

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     *
     * @param buffer byte buffer to write the audio data.
     * @return the size of buffer.
     */
    int read(byte[] buffer);

    /**
     * Release the resources. This is called once at the end.
     */
    void release();
}
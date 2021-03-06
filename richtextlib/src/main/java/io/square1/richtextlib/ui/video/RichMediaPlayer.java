/*
 * Copyright (c) 2015. Roberto  Prato <https://github.com/robertoprato>
 *
 *  *
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package io.square1.richtextlib.ui.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Created by roberto on 12/10/15.
 */
public class RichMediaPlayer implements MediaPlayer.OnPreparedListener {



    public interface OnCompletionListener {
        void onCompletion(RichMediaPlayer mp);
    }

    public interface FirstFrameAvailableListener {
       void onFirstFrameAvailable(RichMediaPlayer player);
    }


    public FirstFrameAvailableListener getFirstFrameAvailableListener() {
        return mFirstFrameAvailableListener;
    }

    public void setFirstFrameAvailableListener(FirstFrameAvailableListener firstFrameAvailableListener) {
        mFirstFrameAvailableListener = firstFrameAvailableListener;
    }

    public FirstFrameAvailableListener mFirstFrameAvailableListener;


    private OnCompletionListener mOnCompletionListener;

    public void setOnCompletionListener(OnCompletionListener listener) {
     mOnCompletionListener = listener;
    }

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;



    private static int MEDIA_PREPARED = 1;
    private static int MEDIA_WAITING = 2;


    private static int PLAYBACK_PLAY = 1;
    private static int PLAYBACK_SHOW_FIRST_FRAME = 2;
    private static int PLAYBACK_STOP = 3;



    @Override
    public void onPrepared(MediaPlayer mp) {
        mCurrentMedia.state = MEDIA_PREPARED;
        mCurrentMedia.height = mp.getVideoHeight();
        mCurrentMedia.width = mp.getVideoWidth();
        // if it is not playing we want to show the first frame
        if(mCurrentMedia.playbackState != PLAYBACK_PLAY) {
            mCurrentMedia.playbackState = PLAYBACK_SHOW_FIRST_FRAME;
        }
        syncMediaState();
    }

    public void release() {

        mCurrentMedia.state = MEDIA_WAITING;
        mCurrentMedia.playbackState = PLAYBACK_STOP;
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    private static class MediaState {

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MediaState that = (MediaState) o;

            return uri.equals(that.uri);

        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        MediaState(Uri uri){
            this.uri = uri;
            this.state = MEDIA_WAITING;
            this.playbackState = PLAYBACK_SHOW_FIRST_FRAME;
        }

        final Uri uri;
        int state;
        int playbackState;
        int width;
        int height;
    }

    private Context mApplicationContext;

    public RichMediaPlayer(Context context){
        mApplicationContext = context.getApplicationContext();
        initMediaPlayer();
        mCurrentMedia = new MediaState(Uri.EMPTY);
    }

    private void initMediaPlayer(){
        if(mMediaPlayer == null) {
            mMediaPlayer = new InternalMediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mOnCompletionListener != null){
                        mOnCompletionListener.onCompletion(RichMediaPlayer.this);
                    }
                }
            });

            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        }
    }

    private InternalMediaPlayer mMediaPlayer;
    private SurfaceTexture mSurfaceTexture;
    private MediaState mCurrentMedia;

    public void setSurfaceTexture(SurfaceTexture texture){

        initMediaPlayer();

        if(texture != mSurfaceTexture){
            mMediaPlayer.setSurface(new Surface(texture));
            mSurfaceTexture = texture;
        }

        syncMediaState();
    }

    public void onSurfaceTextureDestroyed(SurfaceTexture texture){

        initMediaPlayer();

        if(texture == mSurfaceTexture){
            mMediaPlayer.setSurface(null);
            mSurfaceTexture = null;
        }
    }


    public boolean setData(Uri uri){

        initMediaPlayer();

        MediaState mediaState = new MediaState(uri);

        if(mediaState.equals(mCurrentMedia) == false) {

            try {
                mMediaPlayer.setDataSource(mApplicationContext, uri);
                mCurrentMedia = mediaState;
                mMediaPlayer.prepareAsync();
                return true;
            } catch (Exception exc) {
                mCurrentMedia = new MediaState(Uri.EMPTY);
                return false;
            }
        }

        return false;
    }


    public void start(){
        mCurrentMedia.playbackState = PLAYBACK_PLAY;
        syncMediaState();
    }

    public void pause(){
        mCurrentMedia.playbackState = PLAYBACK_STOP;
        syncMediaState();
    }

    /**
     * Will play the media if all conditions are met
     */
    private void syncMediaState(){

        if(mCurrentMedia.playbackState == PLAYBACK_SHOW_FIRST_FRAME &&
                mediaPrepared() &&
                hasSurface() &&
                isPlaying() == false){

            new ShowFrameSeekCompleteListener(0, true, this);
        }
        if(mCurrentMedia.playbackState == PLAYBACK_PLAY &&
                mediaPrepared() &&
                hasSurface() &&
                isPlaying() == false) {

            new ShowFrameSeekCompleteListener(0, false, this);

        }else if(mCurrentMedia.playbackState == PLAYBACK_STOP){
            mMediaPlayer.pause();

        }

    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener){
        if(mMediaPlayer != null) {
            mMediaPlayer.setOnBufferingUpdateListener(listener);
        }
        mOnBufferingUpdateListener = listener;
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public boolean mediaPrepared(){
        return mCurrentMedia.state == MEDIA_PREPARED;
    }

    public boolean hasSurface(){
        return mSurfaceTexture != null;
    }

    public int getVideoHeight(){
        return mCurrentMedia.height;
    }

    public int getVideoWidth(){
        return mCurrentMedia.width;
    }

    private static class ShowFrameSeekCompleteListener implements MediaPlayer.OnSeekCompleteListener {

        private RichMediaPlayer mRichMediaPlayer;
        private MediaPlayer.OnSeekCompleteListener mInitialSeekListener;
        private boolean mStopAfter;

        public ShowFrameSeekCompleteListener(int ms, boolean stopAfter, RichMediaPlayer player){
            mRichMediaPlayer = player;
            mStopAfter = stopAfter;
            mInitialSeekListener = player.mMediaPlayer.getSeekListener();
            player.mMediaPlayer.setOnSeekCompleteListener(this);
            player.mMediaPlayer.start();
            player.mMediaPlayer.seekTo(ms);
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if(mStopAfter == true) {
                mp.pause();
            }
            //restore original seek listener
            mp.setOnSeekCompleteListener(mInitialSeekListener);
            if(mRichMediaPlayer.mFirstFrameAvailableListener != null) {
                mRichMediaPlayer.mFirstFrameAvailableListener.onFirstFrameAvailable(mRichMediaPlayer);
            }
        }
    }

}

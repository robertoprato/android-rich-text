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

package io.square1.richtextlib.spans;

import android.os.Parcel;
import android.os.Parcelable;

import io.square1.parcelable.DynamicParcelable;
import io.square1.richtextlib.ui.RichContentViewDisplay;
import io.square1.richtextlib.util.Utils;

/**
 * Created by roberto on 12/06/15.
 */
public interface RichTextSpan extends DynamicParcelable {

    public  static  final  Parcelable.Creator<RichTextSpan> CREATOR  = new Parcelable.Creator<RichTextSpan>() {


        @Override
        public  RichTextSpan createFromParcel(Parcel source) {
            String type = source.readString();
            RichTextSpan obj = Utils.newInstance(type);
            obj.readFromParcel(source);
            return obj;
        }

        @Override
        public RichTextSpan[] newArray(int size) {
            return new RichTextSpan[size];
        }
    };

    int getType();
   /// void setType(int type);


    void onSpannedSetToView(RichContentViewDisplay view);
    void onAttachedToView(RichContentViewDisplay view);
    void onDetachedFromView(RichContentViewDisplay view);

}

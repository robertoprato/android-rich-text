package io.square1.richtextlib.spans;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import io.square1.parcelable.DynamicParcelableCreator;
import io.square1.richtextlib.ui.RichContentViewDisplay;
import io.square1.richtextlib.util.UniqueId;

public class UnderlineSpan extends CharacterStyle implements UpdateAppearance, RichTextSpan {

 public static final Parcelable.Creator<UnderlineSpan> CREATOR  = DynamicParcelableCreator.getInstance(UnderlineSpan.class);

   public static final int TYPE = UniqueId.getType();

   @Override
   public int getType() {
      return TYPE;
   }

    public UnderlineSpan() {
    }
    
    public UnderlineSpan(Parcel src) {
        readFromParcel(src);
    }

    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        DynamicParcelableCreator.writeType(dest, this);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(true);
    }

    @Override
    public void readFromParcel(Parcel src) {

    }

    @Override
    public void onAttachedToView(RichContentViewDisplay view) {

    }

    @Override
    public void onDetachedFromView(RichContentViewDisplay view) {

    }

    @Override
    public void onSpannedSetToView(RichContentViewDisplay view){

    }
}
package sfotakos.anightout.newevent;

import java.security.InvalidParameterException;

import sfotakos.anightout.R;

public class GooglePlacesRequestParams {

    private String mType = PlaceType.RESTAURANT.getTag();
    private String mPrice = PlacePrice.VERYEXPENSIVE.getTag();

   // TODO make description strings.xml resources

    public enum PlaceType {

        RESTAURANT(R.drawable.ic_store, "Restaurant", "restaurant"),
        BAR(R.drawable.ic_bar, "Bar", "bar"),
        CAFE(R.drawable.ic_cafe, "Cafe", "cafe");

        private String description;
        private String tag;
        private int iconResId;

        PlaceType(int iconResId, String description, String tag){
            this.iconResId = iconResId;
            this.description = description;
            this.tag = tag;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public int getIconResId() {
            return iconResId;
        }

        public void setIconResId(int iconResId) {
            this.iconResId = iconResId;
        }
    }

    public enum PlacePrice {

        FREE("Free", "0"),
        CHEAP("Cheap", "1"),
        MODERATE("Moderate", "2"),
        EXPENSIVE("Expensive", "3"),
        VERYEXPENSIVE("Very Expensive", "4");

        private String description;
        private String tag;

        PlacePrice(String description, String tag){
          this.description = description;
          this.tag = tag;
        }

        public static String getDescriptionByTag(String tag){
            for (PlacePrice placePrice : PlacePrice.values()){
                if (placePrice.tag.equals(tag))
                    return placePrice.description;
            }
            throw new InvalidParameterException();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String mPrice) {
        this.mPrice = mPrice;
    }
}

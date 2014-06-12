package com.hurix.model;

import java.awt.geom.Rectangle2D;

public abstract class Block {
   
   Rectangle2D.Double bbox;

   public Rectangle2D.Double getBbox() {
      return bbox;
   }

   public void setBbox(Rectangle2D.Double bbox) {
      this.bbox = bbox;
   }

}

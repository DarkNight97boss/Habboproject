package com.eu.habbo.util.imager.badges;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildPart;
import com.eu.habbo.habbohotel.guilds.GuildPartType;
import gnu.trove.map.hash.THashMap;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/imager/badges/BadgeImager.class */
public class BadgeImager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadgeImager.class);
    final THashMap<String, BufferedImage> cachedImages = new THashMap<>();

    public BadgeImager() {
        if (Emulator.getConfig().getBoolean("imager.internal.enabled")) {
            if (reload()) {
                LOGGER.info("Badge Imager -> Loaded!");
            } else {
                LOGGER.warn("Badge Imager -> Disabled! Please check your configuration!");
            }
        }
    }

    public static BufferedImage deepCopy(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }
        ColorModel colorModel = bufferedImage.getColorModel();
        return new BufferedImage(colorModel, bufferedImage.copyData((WritableRaster) null), colorModel.isAlphaPremultiplied(), (Hashtable) null);
    }

    public static void recolor(BufferedImage bufferedImage, Color color) {
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int i2 = 0; i2 < bufferedImage.getHeight(); i2++) {
                int rgb = bufferedImage.getRGB(i, i2);
                if ((rgb >> 24) != 0) {
                    Color color2 = new Color(rgb);
                    bufferedImage.setRGB(i, i2, new Color((color2.getRed() / 255.0f) * (color.getRed() / 255.0f), (color2.getGreen() / 255.0f) * (color.getGreen() / 255.0f), (color2.getBlue() / 255.0f) * (color.getBlue() / 255.0f), (color2.getAlpha() / 255.0f) * (color.getAlpha() / 255.0f)).getRGB());
                }
            }
        }
    }

    public static Color colorFromHexString(String str) {
        try {
            return new Color(Integer.valueOf(str, 16).intValue());
        } catch (Exception e) {
            return new Color(16777215);
        }
    }

    public static Point getPoint(BufferedImage bufferedImage, BufferedImage bufferedImage2, int i) {
        int width = 0;
        int height = 0;
        if (i == 1) {
            width = (bufferedImage.getWidth() - bufferedImage2.getWidth()) / 2;
            height = 0;
        } else if (i == 2) {
            width = bufferedImage.getWidth() - bufferedImage2.getWidth();
            height = 0;
        } else if (i == 3) {
            width = 0;
            height = (bufferedImage.getHeight() / 2) - (bufferedImage2.getHeight() / 2);
        } else if (i == 4) {
            width = (bufferedImage.getWidth() / 2) - (bufferedImage2.getWidth() / 2);
            height = (bufferedImage.getHeight() / 2) - (bufferedImage2.getHeight() / 2);
        } else if (i == 5) {
            width = bufferedImage.getWidth() - bufferedImage2.getWidth();
            height = (bufferedImage.getHeight() / 2) - (bufferedImage2.getHeight() / 2);
        } else if (i == 6) {
            width = 0;
            height = bufferedImage.getHeight() - bufferedImage2.getHeight();
        } else if (i == 7) {
            width = (bufferedImage.getWidth() - bufferedImage2.getWidth()) / 2;
            height = bufferedImage.getHeight() - bufferedImage2.getHeight();
        } else if (i == 8) {
            width = bufferedImage.getWidth() - bufferedImage2.getWidth();
            height = bufferedImage.getHeight() - bufferedImage2.getHeight();
        }
        return new Point(width, height);
    }

    public static BufferedImage convert32(BufferedImage bufferedImage) {
        BufferedImage bufferedImage2 = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), 2);
        return new ColorConvertOp(bufferedImage.getColorModel().getColorSpace(), bufferedImage2.getColorModel().getColorSpace(), (RenderingHints) null).filter(bufferedImage, bufferedImage2);
    }

    public synchronized boolean reload() {
        if (!new File(Emulator.getConfig().getValue("imager.location.badgeparts")).exists()) {
            LOGGER.error("BadgeImager output folder: {} does not exist!", Emulator.getConfig().getValue("imager.location.badgeparts"));
            return false;
        }
        this.cachedImages.clear();
        try {
            for (Map.Entry entry : Emulator.getGameEnvironment().getGuildManager().getGuildParts().entrySet()) {
                if (entry.getKey() == GuildPartType.SYMBOL || entry.getKey() == GuildPartType.BASE) {
                    for (Map.Entry entry2 : ((THashMap) entry.getValue()).entrySet()) {
                        if (!((GuildPart) entry2.getValue()).valueA.isEmpty()) {
                            try {
                                this.cachedImages.put(((GuildPart) entry2.getValue()).valueA, ImageIO.read(new File(Emulator.getConfig().getValue("imager.location.badgeparts"), "badgepart_" + ((GuildPart) entry2.getValue()).valueA.replace(".gif", ".png"))));
                            } catch (Exception e) {
                                LOGGER.info("[Badge Imager] Missing Badge Part: " + Emulator.getConfig().getValue("imager.location.badgeparts") + "/badgepart_" + ((GuildPart) entry2.getValue()).valueA.replace(".gif", ".png"));
                            }
                        }
                        if (!((GuildPart) entry2.getValue()).valueB.isEmpty()) {
                            try {
                                this.cachedImages.put(((GuildPart) entry2.getValue()).valueB, ImageIO.read(new File(Emulator.getConfig().getValue("imager.location.badgeparts"), "badgepart_" + ((GuildPart) entry2.getValue()).valueB.replace(".gif", ".png"))));
                            } catch (Exception e2) {
                                LOGGER.info("[Badge Imager] Missing Badge Part: " + Emulator.getConfig().getValue("imager.location.badgeparts") + "/badgepart_" + ((GuildPart) entry2.getValue()).valueB.replace(".gif", ".png"));
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e3) {
            LOGGER.error("Caught exception", e3);
            return false;
        }
    }

    public void generate(Guild guild) {
        String badge = guild.getBadge();
        try {
            File file = new File(Emulator.getConfig().getValue("imager.location.output.badges"), badge + ".png");
            if (file.exists()) {
                return;
            }
            String[] strArr = new String[5];
            strArr[0] = Emulator.PREVIEW;
            strArr[1] = Emulator.PREVIEW;
            strArr[2] = Emulator.PREVIEW;
            strArr[3] = Emulator.PREVIEW;
            strArr[4] = Emulator.PREVIEW;
            int i = 0;
            int i2 = 0;
            while (i2 < badge.length()) {
                if (i2 > 0 && i2 % 7 == 0) {
                    i++;
                }
                for (int i3 = 0; i3 < 7; i3++) {
                    int i4 = i;
                    strArr[i4] = strArr[i4] + badge.charAt(i2);
                    i2++;
                }
            }
            BufferedImage bufferedImage = new BufferedImage(39, 39, 2);
            Graphics graphics = bufferedImage.getGraphics();
            for (String str : strArr) {
                if (!str.isEmpty()) {
                    String str2 = str.charAt(0) + Emulator.PREVIEW;
                    int iIntValue = Integer.valueOf(str.substring(1, 4)).intValue();
                    int iIntValue2 = Integer.valueOf(str.substring(4, 6)).intValue();
                    int iIntValue3 = Integer.valueOf(str.substring(6)).intValue();
                    GuildPart part = Emulator.getGameEnvironment().getGuildManager().getPart(GuildPartType.BASE_COLOR, iIntValue2);
                    GuildPart part2 = str2.equalsIgnoreCase("b") ? Emulator.getGameEnvironment().getGuildManager().getPart(GuildPartType.BASE, iIntValue) : Emulator.getGameEnvironment().getGuildManager().getPart(GuildPartType.SYMBOL, iIntValue);
                    if (part2 != null) {
                        BufferedImage bufferedImageDeepCopy = deepCopy((BufferedImage) this.cachedImages.get(part2.valueA));
                        if (bufferedImageDeepCopy != null) {
                            if (bufferedImageDeepCopy.getColorModel().getPixelSize() < 32) {
                                bufferedImageDeepCopy = convert32(bufferedImageDeepCopy);
                            }
                            Point point = getPoint(bufferedImage, bufferedImageDeepCopy, iIntValue3);
                            recolor(bufferedImageDeepCopy, colorFromHexString(part.valueA));
                            graphics.drawImage(bufferedImageDeepCopy, point.x, point.y, (ImageObserver) null);
                        }
                        if (!part2.valueB.isEmpty()) {
                            BufferedImage bufferedImageDeepCopy2 = deepCopy((BufferedImage) this.cachedImages.get(part2.valueB));
                            if (bufferedImageDeepCopy2 != null) {
                                if (bufferedImageDeepCopy2.getColorModel().getPixelSize() < 32) {
                                    bufferedImageDeepCopy2 = convert32(bufferedImageDeepCopy2);
                                }
                                Point point2 = getPoint(bufferedImage, bufferedImageDeepCopy2, iIntValue3);
                                graphics.drawImage(bufferedImageDeepCopy2, point2.x, point2.y, (ImageObserver) null);
                            }
                        }
                    }
                }
            }
            try {
                ImageIO.write(bufferedImage, "PNG", file);
            } catch (Exception e) {
                LOGGER.error("Failed to generate guild badge: {}.png Make sure the output folder exists and is writable!", file);
            }
            graphics.dispose();
        } catch (Exception e2) {
            LOGGER.error("Caught exception", e2);
        }
    }
}

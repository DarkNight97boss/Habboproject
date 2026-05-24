package com.eu.habbo.habbohotel.catalog.marketplace;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestOffersEvent;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceBuyErrorComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceCancelSaleComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.plugin.events.marketplace.MarketPlaceItemCancelledEvent;
import com.eu.habbo.plugin.events.marketplace.MarketPlaceItemOfferedEvent;
import com.eu.habbo.plugin.events.marketplace.MarketPlaceItemSoldEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketPlace {
   private static final Logger LOGGER = LoggerFactory.getLogger(MarketPlace.class);
   public static boolean MARKETPLACE_ENABLED = true;
   public static int MARKETPLACE_CURRENCY = 0;

   public static THashSet<MarketPlaceOffer> getOwnOffers(Habbo habbo) {
      THashSet<MarketPlaceOffer> offers = new THashSet();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT items_base.type AS type, items.item_id AS base_item_id, items.limited_data AS ltd_data, marketplace_items.* FROM marketplace_items INNER JOIN items ON marketplace_items.item_id = items.id INNER JOIN items_base ON items.item_id = items_base.id WHERE marketplace_items.user_id = ?"
            );

            try {
               statement.setInt(1, habbo.getHabboInfo().getId());
               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     offers.add(new MarketPlaceOffer(set, true));
                  }
               } catch (Throwable var10) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var12.addSuppressed(var7);
               }
            }

            throw var12;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return offers;
   }

   public static void takeBackItem(Habbo habbo, int offerId) {
      MarketPlaceOffer offer = habbo.getInventory().getOffer(offerId);
      if (!Emulator.getPluginManager().fireEvent(new MarketPlaceItemCancelledEvent(offer)).isCancelled()) {
         takeBackItem(habbo, offer);
      }
   }

   private static void takeBackItem(Habbo habbo, MarketPlaceOffer offer) {
      if (offer != null && habbo.getInventory().getMarketplaceItems().contains(offer)) {
         RequestOffersEvent.cachedResults.clear();

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            label222: {
               try {
                  PreparedStatement ownerCheck;
                  label203: {
                     ownerCheck = connection.prepareStatement("SELECT user_id FROM marketplace_items WHERE id = ?", 1004, 1007);

                     try {
                        ownerCheck.setInt(1, offer.getOfferId());
                        ResultSet ownerSet = ownerCheck.executeQuery();

                        label183: {
                           try {
                              ownerSet.last();
                              if (ownerSet.getRow() == 0) {
                                 break label183;
                              }

                              PreparedStatement statement = connection.prepareStatement("DELETE FROM marketplace_items WHERE id = ? AND state != 2");

                              try {
                                 statement.setInt(1, offer.getOfferId());
                                 int count = statement.executeUpdate();
                                 if (count != 0) {
                                    habbo.getInventory().removeMarketplaceOffer(offer);
                                    PreparedStatement updateItems = connection.prepareStatement("UPDATE items SET user_id = ? WHERE id = ? LIMIT 1");

                                    try {
                                       updateItems.setInt(1, habbo.getHabboInfo().getId());
                                       updateItems.setInt(2, offer.getSoldItemId());
                                       updateItems.execute();
                                       PreparedStatement selectItem = connection.prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1");

                                       try {
                                          selectItem.setInt(1, offer.getSoldItemId());
                                          ResultSet set = selectItem.executeQuery();

                                          try {
                                             while (set.next()) {
                                                HabboItem item = Emulator.getGameEnvironment().getItemManager().loadHabboItem(set);
                                                habbo.getInventory().getItemsComponent().addItem(item);
                                                habbo.getClient().sendResponse(new MarketplaceCancelSaleComposer(offer, true));
                                                habbo.getClient().sendResponse(new AddHabboItemComposer(item));
                                                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                                             }
                                          } catch (Throwable var19) {
                                             if (set != null) {
                                                try {
                                                   set.close();
                                                } catch (Throwable var18) {
                                                   var19.addSuppressed(var18);
                                                }
                                             }

                                             throw var19;
                                          }

                                          if (set != null) {
                                             set.close();
                                          }
                                       } catch (Throwable var20) {
                                          if (selectItem != null) {
                                             try {
                                                selectItem.close();
                                             } catch (Throwable var17) {
                                                var20.addSuppressed(var17);
                                             }
                                          }

                                          throw var20;
                                       }

                                       if (selectItem != null) {
                                          selectItem.close();
                                       }
                                    } catch (Throwable var21) {
                                       if (updateItems != null) {
                                          try {
                                             updateItems.close();
                                          } catch (Throwable var16) {
                                             var21.addSuppressed(var16);
                                          }
                                       }

                                       throw var21;
                                    }

                                    if (updateItems != null) {
                                       updateItems.close();
                                    }
                                 }
                              } catch (Throwable var22) {
                                 if (statement != null) {
                                    try {
                                       statement.close();
                                    } catch (Throwable var15) {
                                       var22.addSuppressed(var15);
                                    }
                                 }

                                 throw var22;
                              }

                              if (statement != null) {
                                 statement.close();
                              }
                           } catch (Throwable var23) {
                              if (ownerSet != null) {
                                 try {
                                    ownerSet.close();
                                 } catch (Throwable var14) {
                                    var23.addSuppressed(var14);
                                 }
                              }

                              throw var23;
                           }

                           if (ownerSet != null) {
                              ownerSet.close();
                           }
                           break label203;
                        }

                        if (ownerSet != null) {
                           ownerSet.close();
                        }
                     } catch (Throwable var24) {
                        if (ownerCheck != null) {
                           try {
                              ownerCheck.close();
                           } catch (Throwable var13) {
                              var24.addSuppressed(var13);
                           }
                        }

                        throw var24;
                     }

                     if (ownerCheck != null) {
                        ownerCheck.close();
                     }
                     break label222;
                  }

                  if (ownerCheck != null) {
                     ownerCheck.close();
                  }
               } catch (Throwable var25) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var12) {
                        var25.addSuppressed(var12);
                     }
                  }

                  throw var25;
               }

               if (connection != null) {
                  connection.close();
               }

               return;
            }

            if (connection != null) {
               connection.close();
            }

            return;
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            habbo.getClient().sendResponse(new MarketplaceCancelSaleComposer(offer, false));
         }
      }
   }

   public static List<MarketPlaceOffer> getOffers(int minPrice, int maxPrice, String search, int sort) {
      List<MarketPlaceOffer> offers = new ArrayList<>(10);
      String query = "SELECT B.* FROM marketplace_items a INNER JOIN (SELECT b.item_id AS base_item_id, b.limited_data AS ltd_data, marketplace_items.*, AVG(price) as avg, MIN(marketplace_items.price) as minPrice, MAX(marketplace_items.price) as maxPrice, COUNT(*) as number, (SELECT COUNT(*) FROM marketplace_items c INNER JOIN items as items_b ON c.item_id = items_b.id WHERE state = 2 AND items_b.item_id = base_item_id AND DATE(from_unixtime(sold_timestamp)) = CURDATE()) as sold_count_today FROM marketplace_items INNER JOIN items b ON marketplace_items.item_id = b.id INNER JOIN items_base bi ON b.item_id = bi.id INNER JOIN catalog_items ci ON bi.id = ci.item_ids WHERE price = (SELECT MIN(e.price) FROM marketplace_items e, items d WHERE e.item_id = d.id AND d.item_id = b.item_id AND e.state = 1 AND e.timestamp > ? GROUP BY d.item_id) AND state = 1 AND timestamp > ?";
      if (minPrice > 0) {
         query = query + " AND CEIL(price + (price / 100)) >= " + minPrice;
      }

      if (maxPrice > 0 && maxPrice > minPrice) {
         query = query + " AND CEIL(price + (price / 100)) <= " + maxPrice;
      }

      if (search.length() > 0) {
         query = query + " AND ( bi.public_name LIKE ? OR ci.catalog_name LIKE ? ) ";
      }

      query = query + " GROUP BY base_item_id, ltd_data";
      switch (sort) {
         case 1:
         default:
            query = query + " ORDER BY minPrice DESC";
            break;
         case 2:
            query = query + " ORDER BY minPrice ASC";
            break;
         case 3:
            query = query + " ORDER BY sold_count_today DESC";
            break;
         case 4:
            query = query + " ORDER BY sold_count_today ASC";
            break;
         case 5:
            query = query + " ORDER BY number DESC";
            break;
         case 6:
            query = query + " ORDER BY number ASC";
      }

      query = query + ")";
      query = query + " AS B ON a.id = B.id";
      query = query + " LIMIT 250";

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(query);

            try {
               statement.setInt(1, Emulator.getIntUnixTimestamp() - 172800);
               statement.setInt(2, Emulator.getIntUnixTimestamp() - 172800);
               if (search.length() > 0) {
                  statement.setString(3, "%" + search + "%");
                  statement.setString(4, "%" + search + "%");
               }

               ResultSet set = statement.executeQuery();

               try {
                  while (set.next()) {
                     offers.add(new MarketPlaceOffer(set, false));
                  }
               } catch (Throwable var14) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }
                  }

                  throw var14;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var15) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var12) {
                     var15.addSuppressed(var12);
                  }
               }

               throw var15;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var16) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var11) {
                  var16.addSuppressed(var11);
               }
            }

            throw var16;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return offers;
   }

   public static void serializeItemInfo(int itemId, ServerMessage message) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT avg(marketplace_items.price) as price, COUNT(*) as sold, (datediff(NOW(), DATE(from_unixtime(marketplace_items.timestamp)))) as day FROM marketplace_items INNER JOIN items ON items.id = marketplace_items.item_id INNER JOIN items_base ON items.item_id = items_base.id WHERE items.limited_data = '0:0' AND marketplace_items.state = 2 AND items_base.sprite_id = ? AND DATE(from_unixtime(marketplace_items.timestamp)) >= NOW() - INTERVAL 30 DAY GROUP BY DATE(from_unixtime(marketplace_items.timestamp))",
               1004,
               1007
            );

            try {
               statement.setInt(1, itemId);
               message.appendInt(avarageLastXDays(itemId, 7));
               message.appendInt(itemsOnSale(itemId));
               message.appendInt(30);
               ResultSet set = statement.executeQuery();

               try {
                  set.last();
                  message.appendInt(set.getRow());
                  set.beforeFirst();

                  while (set.next()) {
                     message.appendInt(-set.getInt("day"));
                     message.appendInt(calculateCommision(set.getInt("price")));
                     message.appendInt(set.getInt("sold"));
                  }
               } catch (Throwable var10) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (set != null) {
                  set.close();
               }

               message.appendInt(1);
               message.appendInt(itemId);
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var12.addSuppressed(var7);
               }
            }

            throw var12;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public static int itemsOnSale(int baseItemId) {
      int number = 0;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT COUNT(*) as number, AVG(price) as avg FROM marketplace_items INNER JOIN items ON marketplace_items.item_id = items.id INNER JOIN items_base ON items.item_id = items_base.id WHERE state = 1 AND timestamp >= ? AND items_base.sprite_id = ?",
               1004,
               1007
            );

            try {
               statement.setInt(1, Emulator.getIntUnixTimestamp() - 172800);
               statement.setInt(2, baseItemId);
               ResultSet set = statement.executeQuery();

               try {
                  set.first();
                  number = set.getInt("number");
               } catch (Throwable var10) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var12.addSuppressed(var7);
               }
            }

            throw var12;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return number;
   }

   private static int avarageLastXDays(int baseItemId, int days) {
      int avg = 0;

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT AVG(price) as avg FROM marketplace_items INNER JOIN items ON marketplace_items.item_id = items.id INNER JOIN items_base ON items.item_id = items_base.id WHERE state = 2 AND DATE(from_unixtime(timestamp)) >= NOW() - INTERVAL ? DAY AND items_base.sprite_id = ?",
               1004,
               1007
            );

            try {
               statement.setInt(1, days);
               statement.setInt(2, baseItemId);
               ResultSet set = statement.executeQuery();

               try {
                  set.first();
                  avg = set.getInt("avg");
               } catch (Throwable var11) {
                  if (set != null) {
                     try {
                        set.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (set != null) {
                  set.close();
               }
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      return calculateCommision(avg);
   }

   public static void buyItem(int offerId, GameClient client) {
      RequestOffersEvent.cachedResults.clear();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         label234: {
            try {
               PreparedStatement statement;
               label216: {
                  statement = connection.prepareStatement("SELECT * FROM marketplace_items WHERE id = ? LIMIT 1");

                  try {
                     statement.setInt(1, offerId);
                     ResultSet set = statement.executeQuery();

                     label196: {
                        try {
                           label160:
                           if (set.next()) {
                              PreparedStatement itemStatement = connection.prepareStatement("SELECT * FROM items WHERE id = ? LIMIT 1", 1004, 1007);

                              label192: {
                                 try {
                                    itemStatement.setInt(1, set.getInt("item_id"));
                                    ResultSet itemSet = itemStatement.executeQuery();

                                    label185: {
                                       try {
                                          label230: {
                                             itemSet.first();
                                             if (itemSet.getRow() <= 0) {
                                                break label185;
                                             }

                                             int price = calculateCommision(set.getInt("price"));
                                             if (set.getInt("state") != 1) {
                                                sendErrorMessage(client, set.getInt("item_id"), offerId);
                                                break label185;
                                             }

                                             if ((MARKETPLACE_CURRENCY != 0 || price <= client.getHabbo().getHabboInfo().getCredits())
                                                && (
                                                   MARKETPLACE_CURRENCY <= 0
                                                      || price <= client.getHabbo().getHabboInfo().getCurrencyAmount(MARKETPLACE_CURRENCY)
                                                )) {
                                                PreparedStatement updateOffer = connection.prepareStatement(
                                                   "UPDATE marketplace_items SET state = 2, sold_timestamp = ? WHERE id = ?"
                                                );

                                                try {
                                                   updateOffer.setInt(1, Emulator.getIntUnixTimestamp());
                                                   updateOffer.setInt(2, offerId);
                                                   updateOffer.execute();
                                                } catch (Throwable var17) {
                                                   if (updateOffer != null) {
                                                      try {
                                                         updateOffer.close();
                                                      } catch (Throwable var16) {
                                                         var17.addSuppressed(var16);
                                                      }
                                                   }

                                                   throw var17;
                                                }

                                                if (updateOffer != null) {
                                                   updateOffer.close();
                                                }

                                                Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(set.getInt("user_id"));
                                                HabboItem item = Emulator.getGameEnvironment().getItemManager().loadHabboItem(itemSet);
                                                MarketPlaceItemSoldEvent event = new MarketPlaceItemSoldEvent(
                                                   habbo, client.getHabbo(), item, set.getInt("price")
                                                );
                                                if (Emulator.getPluginManager().fireEvent(event).isCancelled()) {
                                                   break label230;
                                                }

                                                event.price = calculateCommision(event.price);
                                                item.setUserId(client.getHabbo().getHabboInfo().getId());
                                                item.needsUpdate(true);
                                                Emulator.getThreading().run(item);
                                                client.getHabbo().getInventory().getItemsComponent().addItem(item);
                                                if (MARKETPLACE_CURRENCY == 0) {
                                                   client.getHabbo().giveCredits(-event.price);
                                                } else {
                                                   client.getHabbo().givePoints(MARKETPLACE_CURRENCY, -event.price);
                                                }

                                                client.sendResponse(new AddHabboItemComposer(item));
                                                client.sendResponse(new InventoryRefreshComposer());
                                                client.sendResponse(new MarketplaceBuyErrorComposer(1, 0, offerId, price));
                                                if (habbo != null) {
                                                   habbo.getInventory().getOffer(offerId).setState(MarketPlaceState.SOLD);
                                                }
                                                break label185;
                                             }

                                             client.sendResponse(new MarketplaceBuyErrorComposer(4, 0, offerId, price));
                                             break label185;
                                          }
                                       } catch (Throwable var18) {
                                          if (itemSet != null) {
                                             try {
                                                itemSet.close();
                                             } catch (Throwable var15) {
                                                var18.addSuppressed(var15);
                                             }
                                          }

                                          throw var18;
                                       }

                                       if (itemSet != null) {
                                          itemSet.close();
                                       }
                                       break label192;
                                    }

                                    if (itemSet != null) {
                                       itemSet.close();
                                    }
                                 } catch (Throwable var19) {
                                    if (itemStatement != null) {
                                       try {
                                          itemStatement.close();
                                       } catch (Throwable var14) {
                                          var19.addSuppressed(var14);
                                       }
                                    }

                                    throw var19;
                                 }

                                 if (itemStatement != null) {
                                    itemStatement.close();
                                 }
                                 break label160;
                              }

                              if (itemStatement != null) {
                                 itemStatement.close();
                              }
                              break label196;
                           }
                        } catch (Throwable var20) {
                           if (set != null) {
                              try {
                                 set.close();
                              } catch (Throwable var13) {
                                 var20.addSuppressed(var13);
                              }
                           }

                           throw var20;
                        }

                        if (set != null) {
                           set.close();
                        }
                        break label216;
                     }

                     if (set != null) {
                        set.close();
                     }
                  } catch (Throwable var21) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var12) {
                           var21.addSuppressed(var12);
                        }
                     }

                     throw var21;
                  }

                  if (statement != null) {
                     statement.close();
                  }
                  break label234;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var22) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var11) {
                     var22.addSuppressed(var11);
                  }
               }

               throw var22;
            }

            if (connection != null) {
               connection.close();
            }

            return;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public static void sendErrorMessage(GameClient client, int baseItemId, int offerId) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement(
               "SELECT marketplace_items.*, COUNT( * ) AS count\nFROM marketplace_items\nINNER JOIN items ON marketplace_items.item_id = items.id\nINNER JOIN items_base ON items.item_id = items_base.id\nWHERE items_base.sprite_id = ( \nSELECT items_base.sprite_id\nFROM items_base\nWHERE items_base.id = ? LIMIT 1)\nORDER BY price ASC\nLIMIT 1",
               1004,
               1007
            );

            try {
               statement.setInt(1, baseItemId);
               ResultSet countSet = statement.executeQuery();

               try {
                  countSet.last();
                  if (countSet.getRow() == 0) {
                     client.sendResponse(new MarketplaceBuyErrorComposer(2, 0, offerId, 0));
                  } else {
                     countSet.first();
                     client.sendResponse(
                        new MarketplaceBuyErrorComposer(3, countSet.getInt("count"), countSet.getInt("id"), calculateCommision(countSet.getInt("price")))
                     );
                  }
               } catch (Throwable var11) {
                  if (countSet != null) {
                     try {
                        countSet.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (countSet != null) {
                  countSet.close();
               }
            } catch (Throwable var12) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var13) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public static boolean sellItem(GameClient client, HabboItem item, int price) {
      if (item == null || client == null) {
         return false;
      }

      if (item.getBaseItem().allowMarketplace() && price >= 0) {
         MarketPlaceItemOfferedEvent event = new MarketPlaceItemOfferedEvent(client.getHabbo(), item, price);
         if (Emulator.getPluginManager().fireEvent(event).isCancelled()) {
            return false;
         }

         RequestOffersEvent.cachedResults.clear();
         client.sendResponse(new RemoveHabboItemComposer(event.item.getGiftAdjustedId()));
         client.sendResponse(new InventoryRefreshComposer());
         event.item.setFromGift(false);
         MarketPlaceOffer offer = new MarketPlaceOffer(event.item, event.price, client.getHabbo());
         client.getHabbo().getInventory().addMarketplaceOffer(offer);
         client.getHabbo().getInventory().getItemsComponent().removeHabboItem(event.item);
         item.setUserId(-1);
         item.needsUpdate(true);
         Emulator.getThreading().run(item);
         return true;
      } else {
         return false;
      }
   }

   public static void getCredits(GameClient client) {
      int credits = 0;
      THashSet<MarketPlaceOffer> offers = new THashSet();
      offers.addAll(client.getHabbo().getInventory().getMarketplaceItems());
      TObjectHashIterator var3 = offers.iterator();

      while (var3.hasNext()) {
         MarketPlaceOffer offer = (MarketPlaceOffer)var3.next();
         if (offer.getState().equals(MarketPlaceState.SOLD)) {
            client.getHabbo().getInventory().removeMarketplaceOffer(offer);
            credits += offer.getPrice();
            removeUser(offer);
            offer.needsUpdate(true);
            Emulator.getThreading().run(offer);
         }
      }

      offers.clear();
      if (MARKETPLACE_CURRENCY == 0) {
         client.getHabbo().giveCredits(credits);
      } else {
         client.getHabbo().givePoints(MARKETPLACE_CURRENCY, credits);
      }
   }

   private static void removeUser(MarketPlaceOffer offer) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            PreparedStatement statement = connection.prepareStatement("UPDATE marketplace_items SET user_id = ? WHERE id = ?");

            try {
               statement.setInt(1, -1);
               statement.setInt(2, offer.getOfferId());
               statement.execute();
            } catch (Throwable var7) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var8) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public static int calculateCommision(int price) {
      return price + (int)Math.ceil(price / 100.0);
   }
}

package com.eu.habbo.messages;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.incoming.achievements.RequestAchievementConfigurationEvent;
import com.eu.habbo.messages.incoming.achievements.RequestAchievementsEvent;
import com.eu.habbo.messages.incoming.ambassadors.AmbassadorAlertCommandEvent;
import com.eu.habbo.messages.incoming.ambassadors.AmbassadorVisitCommandEvent;
import com.eu.habbo.messages.incoming.camera.CameraPublishToWebEvent;
import com.eu.habbo.messages.incoming.camera.CameraPurchaseEvent;
import com.eu.habbo.messages.incoming.camera.CameraRoomPictureEvent;
import com.eu.habbo.messages.incoming.camera.CameraRoomThumbnailEvent;
import com.eu.habbo.messages.incoming.camera.RequestCameraConfigurationEvent;
import com.eu.habbo.messages.incoming.catalog.CatalogBuyClubDiscountEvent;
import com.eu.habbo.messages.incoming.catalog.CatalogBuyItemAsGiftEvent;
import com.eu.habbo.messages.incoming.catalog.CatalogBuyItemEvent;
import com.eu.habbo.messages.incoming.catalog.CatalogRequestClubDiscountEvent;
import com.eu.habbo.messages.incoming.catalog.CatalogSearchedItemEvent;
import com.eu.habbo.messages.incoming.catalog.CatalogSelectClubGiftEvent;
import com.eu.habbo.messages.incoming.catalog.CheckPetNameEvent;
import com.eu.habbo.messages.incoming.catalog.JukeBoxRequestTrackCodeEvent;
import com.eu.habbo.messages.incoming.catalog.JukeBoxRequestTrackDataEvent;
import com.eu.habbo.messages.incoming.catalog.PurchaseTargetOfferEvent;
import com.eu.habbo.messages.incoming.catalog.RedeemVoucherEvent;
import com.eu.habbo.messages.incoming.catalog.RequestCatalogIndexEvent;
import com.eu.habbo.messages.incoming.catalog.RequestCatalogModeEvent;
import com.eu.habbo.messages.incoming.catalog.RequestCatalogPageEvent;
import com.eu.habbo.messages.incoming.catalog.RequestClubDataEvent;
import com.eu.habbo.messages.incoming.catalog.RequestClubGiftsEvent;
import com.eu.habbo.messages.incoming.catalog.RequestDiscountEvent;
import com.eu.habbo.messages.incoming.catalog.RequestGiftConfigurationEvent;
import com.eu.habbo.messages.incoming.catalog.RequestMarketplaceConfigEvent;
import com.eu.habbo.messages.incoming.catalog.RequestPetBreedsEvent;
import com.eu.habbo.messages.incoming.catalog.TargetOfferStateEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.BuyItemEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestCreditsEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestItemInfoEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestOffersEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestOwnItemsEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.RequestSellItemEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.SellItemEvent;
import com.eu.habbo.messages.incoming.catalog.marketplace.TakeBackItemEvent;
import com.eu.habbo.messages.incoming.catalog.recycler.OpenRecycleBoxEvent;
import com.eu.habbo.messages.incoming.catalog.recycler.RecycleEvent;
import com.eu.habbo.messages.incoming.catalog.recycler.ReloadRecyclerEvent;
import com.eu.habbo.messages.incoming.catalog.recycler.RequestRecyclerLogicEvent;
import com.eu.habbo.messages.incoming.crafting.CraftingAddRecipeEvent;
import com.eu.habbo.messages.incoming.crafting.CraftingCraftItemEvent;
import com.eu.habbo.messages.incoming.crafting.CraftingCraftSecretEvent;
import com.eu.habbo.messages.incoming.crafting.RequestCraftingRecipesAvailableEvent;
import com.eu.habbo.messages.incoming.crafting.RequestCraftingRecipesEvent;
import com.eu.habbo.messages.incoming.events.calendar.AdventCalendarForceOpenEvent;
import com.eu.habbo.messages.incoming.events.calendar.AdventCalendarOpenDayEvent;
import com.eu.habbo.messages.incoming.floorplaneditor.FloorPlanEditorRequestBlockedTilesEvent;
import com.eu.habbo.messages.incoming.floorplaneditor.FloorPlanEditorRequestDoorSettingsEvent;
import com.eu.habbo.messages.incoming.floorplaneditor.FloorPlanEditorSaveEvent;
import com.eu.habbo.messages.incoming.friends.AcceptFriendRequestEvent;
import com.eu.habbo.messages.incoming.friends.ChangeRelationEvent;
import com.eu.habbo.messages.incoming.friends.DeclineFriendRequestEvent;
import com.eu.habbo.messages.incoming.friends.FindNewFriendsEvent;
import com.eu.habbo.messages.incoming.friends.FriendPrivateMessageEvent;
import com.eu.habbo.messages.incoming.friends.FriendRequestEvent;
import com.eu.habbo.messages.incoming.friends.InviteFriendsEvent;
import com.eu.habbo.messages.incoming.friends.RemoveFriendEvent;
import com.eu.habbo.messages.incoming.friends.RequestFriendRequestsEvent;
import com.eu.habbo.messages.incoming.friends.RequestFriendsEvent;
import com.eu.habbo.messages.incoming.friends.RequestInitFriendsEvent;
import com.eu.habbo.messages.incoming.friends.SearchUserEvent;
import com.eu.habbo.messages.incoming.friends.StalkFriendEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterJoinGameEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterLeaveGameEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterLoadGameEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterRequestAccountStatusEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterRequestGameStatusEvent;
import com.eu.habbo.messages.incoming.gamecenter.GameCenterRequestGamesEvent;
import com.eu.habbo.messages.incoming.guardians.GuardianAcceptRequestEvent;
import com.eu.habbo.messages.incoming.guardians.GuardianNoUpdatesWantedEvent;
import com.eu.habbo.messages.incoming.guardians.GuardianVoteEvent;
import com.eu.habbo.messages.incoming.guides.GuideCancelHelpRequestEvent;
import com.eu.habbo.messages.incoming.guides.GuideCloseHelpRequestEvent;
import com.eu.habbo.messages.incoming.guides.GuideHandleHelpRequestEvent;
import com.eu.habbo.messages.incoming.guides.GuideInviteUserEvent;
import com.eu.habbo.messages.incoming.guides.GuideRecommendHelperEvent;
import com.eu.habbo.messages.incoming.guides.GuideReportHelperEvent;
import com.eu.habbo.messages.incoming.guides.GuideUserMessageEvent;
import com.eu.habbo.messages.incoming.guides.GuideUserTypingEvent;
import com.eu.habbo.messages.incoming.guides.GuideVisitUserEvent;
import com.eu.habbo.messages.incoming.guides.RequestGuideAssistanceEvent;
import com.eu.habbo.messages.incoming.guides.RequestGuideToolEvent;
import com.eu.habbo.messages.incoming.guilds.GetHabboGuildBadgesMessageEvent;
import com.eu.habbo.messages.incoming.guilds.GuildAcceptMembershipEvent;
import com.eu.habbo.messages.incoming.guilds.GuildChangeBadgeEvent;
import com.eu.habbo.messages.incoming.guilds.GuildChangeColorsEvent;
import com.eu.habbo.messages.incoming.guilds.GuildChangeNameDescEvent;
import com.eu.habbo.messages.incoming.guilds.GuildChangeSettingsEvent;
import com.eu.habbo.messages.incoming.guilds.GuildConfirmRemoveMemberEvent;
import com.eu.habbo.messages.incoming.guilds.GuildDeclineMembershipEvent;
import com.eu.habbo.messages.incoming.guilds.GuildDeleteEvent;
import com.eu.habbo.messages.incoming.guilds.GuildRemoveAdminEvent;
import com.eu.habbo.messages.incoming.guilds.GuildRemoveFavoriteEvent;
import com.eu.habbo.messages.incoming.guilds.GuildRemoveMemberEvent;
import com.eu.habbo.messages.incoming.guilds.GuildSetAdminEvent;
import com.eu.habbo.messages.incoming.guilds.GuildSetFavoriteEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildBuyEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildBuyRoomsEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildFurniWidgetEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildInfoEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildJoinEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildManageEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildMembersEvent;
import com.eu.habbo.messages.incoming.guilds.RequestGuildPartsEvent;
import com.eu.habbo.messages.incoming.guilds.RequestOwnGuildsEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumDataEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumListEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumModerateMessageEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumModerateThreadEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumPostThreadEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumThreadUpdateEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumThreadsEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumThreadsMessagesEvent;
import com.eu.habbo.messages.incoming.guilds.forums.GuildForumUpdateSettingsEvent;
import com.eu.habbo.messages.incoming.handshake.CompleteDiffieHandshakeEvent;
import com.eu.habbo.messages.incoming.handshake.InitDiffieHandshakeEvent;
import com.eu.habbo.messages.incoming.handshake.MachineIDEvent;
import com.eu.habbo.messages.incoming.handshake.PingEvent;
import com.eu.habbo.messages.incoming.handshake.ReleaseVersionEvent;
import com.eu.habbo.messages.incoming.handshake.SecureLoginEvent;
import com.eu.habbo.messages.incoming.handshake.UsernameEvent;
import com.eu.habbo.messages.incoming.helper.MySanctionStatusEvent;
import com.eu.habbo.messages.incoming.helper.RequestTalentTrackEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewClaimBadgeRewardEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewDataEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewRequestBadgeRewardEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewRequestBonusRareEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewRequestLTDAvailabilityEvent;
import com.eu.habbo.messages.incoming.hotelview.HotelViewRequestSecondsUntilEvent;
import com.eu.habbo.messages.incoming.hotelview.RequestNewsListEvent;
import com.eu.habbo.messages.incoming.inventory.RequestInventoryBadgesEvent;
import com.eu.habbo.messages.incoming.inventory.RequestInventoryBotsEvent;
import com.eu.habbo.messages.incoming.inventory.RequestInventoryItemsEvent;
import com.eu.habbo.messages.incoming.inventory.RequestInventoryPetsEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolAlertEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolChangeRoomSettingsEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolCloseTicketEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolIssueChangeTopicEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolIssueDefaultSanctionEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolKickEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolPickTicketEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolReleaseTicketEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestIssueChatlogEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestRoomChatlogEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestRoomInfoEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestRoomUserChatlogEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestRoomVisitsEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestUserChatlogEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRequestUserInfoEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolRoomAlertEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolSanctionAlertEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolSanctionBanEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolSanctionMuteEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolSanctionTradeLockEvent;
import com.eu.habbo.messages.incoming.modtool.ModToolWarnEvent;
import com.eu.habbo.messages.incoming.modtool.ReportBullyEvent;
import com.eu.habbo.messages.incoming.modtool.ReportCommentEvent;
import com.eu.habbo.messages.incoming.modtool.ReportEvent;
import com.eu.habbo.messages.incoming.modtool.ReportFriendPrivateChatEvent;
import com.eu.habbo.messages.incoming.modtool.ReportPhotoEvent;
import com.eu.habbo.messages.incoming.modtool.ReportThreadEvent;
import com.eu.habbo.messages.incoming.modtool.RequestReportRoomEvent;
import com.eu.habbo.messages.incoming.modtool.RequestReportUserBullyingEvent;
import com.eu.habbo.messages.incoming.navigator.AddSavedSearchEvent;
import com.eu.habbo.messages.incoming.navigator.DeleteSavedSearchEvent;
import com.eu.habbo.messages.incoming.navigator.NavigatorCategoryListModeEvent;
import com.eu.habbo.messages.incoming.navigator.NavigatorCollapseCategoryEvent;
import com.eu.habbo.messages.incoming.navigator.NavigatorUncollapseCategoryEvent;
import com.eu.habbo.messages.incoming.navigator.NewNavigatorActionEvent;
import com.eu.habbo.messages.incoming.navigator.RequestCanCreateRoomEvent;
import com.eu.habbo.messages.incoming.navigator.RequestCreateRoomEvent;
import com.eu.habbo.messages.incoming.navigator.RequestDeleteRoomEvent;
import com.eu.habbo.messages.incoming.navigator.RequestHighestScoreRoomsEvent;
import com.eu.habbo.messages.incoming.navigator.RequestMyRoomsEvent;
import com.eu.habbo.messages.incoming.navigator.RequestNavigatorSettingsEvent;
import com.eu.habbo.messages.incoming.navigator.RequestNewNavigatorDataEvent;
import com.eu.habbo.messages.incoming.navigator.RequestNewNavigatorRoomsEvent;
import com.eu.habbo.messages.incoming.navigator.RequestPopularRoomsEvent;
import com.eu.habbo.messages.incoming.navigator.RequestPromotedRoomsEvent;
import com.eu.habbo.messages.incoming.navigator.RequestRoomCategoriesEvent;
import com.eu.habbo.messages.incoming.navigator.RequestTagsEvent;
import com.eu.habbo.messages.incoming.navigator.SaveWindowSettingsEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsByTagEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsFriendsNowEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsFriendsOwnEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsInGroupEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsMyFavouriteEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsVisitedEvent;
import com.eu.habbo.messages.incoming.navigator.SearchRoomsWithRightsEvent;
import com.eu.habbo.messages.incoming.polls.AnswerPollEvent;
import com.eu.habbo.messages.incoming.polls.CancelPollEvent;
import com.eu.habbo.messages.incoming.polls.GetPollDataEvent;
import com.eu.habbo.messages.incoming.rooms.HandleDoorbellEvent;
import com.eu.habbo.messages.incoming.rooms.RequestRoomDataEvent;
import com.eu.habbo.messages.incoming.rooms.RequestRoomHeightmapEvent;
import com.eu.habbo.messages.incoming.rooms.RequestRoomLoadEvent;
import com.eu.habbo.messages.incoming.rooms.RequestRoomRightsEvent;
import com.eu.habbo.messages.incoming.rooms.RequestRoomSettingsEvent;
import com.eu.habbo.messages.incoming.rooms.RequestRoomWordFilterEvent;
import com.eu.habbo.messages.incoming.rooms.RoomBackgroundEvent;
import com.eu.habbo.messages.incoming.rooms.RoomFavoriteEvent;
import com.eu.habbo.messages.incoming.rooms.RoomMuteEvent;
import com.eu.habbo.messages.incoming.rooms.RoomPlacePaintEvent;
import com.eu.habbo.messages.incoming.rooms.RoomRemoveAllRightsEvent;
import com.eu.habbo.messages.incoming.rooms.RoomRemoveRightsEvent;
import com.eu.habbo.messages.incoming.rooms.RoomRequestBannedUsersEvent;
import com.eu.habbo.messages.incoming.rooms.RoomSettingsSaveEvent;
import com.eu.habbo.messages.incoming.rooms.RoomStaffPickEvent;
import com.eu.habbo.messages.incoming.rooms.RoomUnFavoriteEvent;
import com.eu.habbo.messages.incoming.rooms.RoomVoteEvent;
import com.eu.habbo.messages.incoming.rooms.RoomWordFilterModifyEvent;
import com.eu.habbo.messages.incoming.rooms.SetHomeRoomEvent;
import com.eu.habbo.messages.incoming.rooms.bots.BotPickupEvent;
import com.eu.habbo.messages.incoming.rooms.bots.BotPlaceEvent;
import com.eu.habbo.messages.incoming.rooms.bots.BotSaveSettingsEvent;
import com.eu.habbo.messages.incoming.rooms.bots.BotSettingsEvent;
import com.eu.habbo.messages.incoming.rooms.items.AdvertisingSaveEvent;
import com.eu.habbo.messages.incoming.rooms.items.CloseDiceEvent;
import com.eu.habbo.messages.incoming.rooms.items.FootballGateSaveLookEvent;
import com.eu.habbo.messages.incoming.rooms.items.MannequinSaveLookEvent;
import com.eu.habbo.messages.incoming.rooms.items.MannequinSaveNameEvent;
import com.eu.habbo.messages.incoming.rooms.items.MoodLightSaveSettingsEvent;
import com.eu.habbo.messages.incoming.rooms.items.MoodLightSettingsEvent;
import com.eu.habbo.messages.incoming.rooms.items.MoodLightTurnOnEvent;
import com.eu.habbo.messages.incoming.rooms.items.MoveWallItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.PostItDeleteEvent;
import com.eu.habbo.messages.incoming.rooms.items.PostItPlaceEvent;
import com.eu.habbo.messages.incoming.rooms.items.PostItRequestDataEvent;
import com.eu.habbo.messages.incoming.rooms.items.PostItSaveDataEvent;
import com.eu.habbo.messages.incoming.rooms.items.RedeemClothingEvent;
import com.eu.habbo.messages.incoming.rooms.items.RedeemItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.RoomPickupItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.RoomPlaceItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.RotateMoveItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.SavePostItStickyPoleEvent;
import com.eu.habbo.messages.incoming.rooms.items.SetStackHelperHeightEvent;
import com.eu.habbo.messages.incoming.rooms.items.ToggleFloorItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.ToggleWallItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.TriggerColorWheelEvent;
import com.eu.habbo.messages.incoming.rooms.items.TriggerDiceEvent;
import com.eu.habbo.messages.incoming.rooms.items.TriggerOneWayGateEvent;
import com.eu.habbo.messages.incoming.rooms.items.UseRandomStateItemEvent;
import com.eu.habbo.messages.incoming.rooms.items.jukebox.JukeBoxAddSoundTrackEvent;
import com.eu.habbo.messages.incoming.rooms.items.jukebox.JukeBoxEventOne;
import com.eu.habbo.messages.incoming.rooms.items.jukebox.JukeBoxEventTwo;
import com.eu.habbo.messages.incoming.rooms.items.jukebox.JukeBoxRemoveSoundTrackEvent;
import com.eu.habbo.messages.incoming.rooms.items.jukebox.JukeBoxRequestPlayListEvent;
import com.eu.habbo.messages.incoming.rooms.items.lovelock.LoveLockStartConfirmEvent;
import com.eu.habbo.messages.incoming.rooms.items.rentablespace.RentSpaceCancelEvent;
import com.eu.habbo.messages.incoming.rooms.items.rentablespace.RentSpaceEvent;
import com.eu.habbo.messages.incoming.rooms.items.youtube.YoutubeRequestPlaylistChange;
import com.eu.habbo.messages.incoming.rooms.items.youtube.YoutubeRequestPlaylists;
import com.eu.habbo.messages.incoming.rooms.items.youtube.YoutubeRequestStateChange;
import com.eu.habbo.messages.incoming.rooms.pets.BreedMonsterplantsEvent;
import com.eu.habbo.messages.incoming.rooms.pets.CompostMonsterplantEvent;
import com.eu.habbo.messages.incoming.rooms.pets.ConfirmPetBreedingEvent;
import com.eu.habbo.messages.incoming.rooms.pets.HorseRemoveSaddleEvent;
import com.eu.habbo.messages.incoming.rooms.pets.MovePetEvent;
import com.eu.habbo.messages.incoming.rooms.pets.PetPackageNameEvent;
import com.eu.habbo.messages.incoming.rooms.pets.PetPickupEvent;
import com.eu.habbo.messages.incoming.rooms.pets.PetPlaceEvent;
import com.eu.habbo.messages.incoming.rooms.pets.PetRideEvent;
import com.eu.habbo.messages.incoming.rooms.pets.PetRideSettingsEvent;
import com.eu.habbo.messages.incoming.rooms.pets.PetUseItemEvent;
import com.eu.habbo.messages.incoming.rooms.pets.RequestPetInformationEvent;
import com.eu.habbo.messages.incoming.rooms.pets.RequestPetTrainingPanelEvent;
import com.eu.habbo.messages.incoming.rooms.pets.ScratchPetEvent;
import com.eu.habbo.messages.incoming.rooms.pets.StopBreedingEvent;
import com.eu.habbo.messages.incoming.rooms.pets.ToggleMonsterplantBreedableEvent;
import com.eu.habbo.messages.incoming.rooms.promotions.BuyRoomPromotionEvent;
import com.eu.habbo.messages.incoming.rooms.promotions.RequestPromotionRoomsEvent;
import com.eu.habbo.messages.incoming.rooms.promotions.UpdateRoomPromotionEvent;
import com.eu.habbo.messages.incoming.rooms.users.IgnoreRoomUserEvent;
import com.eu.habbo.messages.incoming.rooms.users.RequestRoomUserTagsEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserActionEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserBanEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserDanceEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserDropHandItemEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserGiveHandItemEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserGiveRespectEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserGiveRightsEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserKickEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserLookAtPoint;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserMuteEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserRemoveRightsEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserShoutEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserSignEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserSitEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserStartTypingEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserStopTypingEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserTalkEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserWalkEvent;
import com.eu.habbo.messages.incoming.rooms.users.RoomUserWhisperEvent;
import com.eu.habbo.messages.incoming.rooms.users.UnIgnoreRoomUserEvent;
import com.eu.habbo.messages.incoming.rooms.users.UnbanRoomUserEvent;
import com.eu.habbo.messages.incoming.trading.TradeAcceptEvent;
import com.eu.habbo.messages.incoming.trading.TradeCancelEvent;
import com.eu.habbo.messages.incoming.trading.TradeCancelOfferItemEvent;
import com.eu.habbo.messages.incoming.trading.TradeCloseEvent;
import com.eu.habbo.messages.incoming.trading.TradeConfirmEvent;
import com.eu.habbo.messages.incoming.trading.TradeOfferItemEvent;
import com.eu.habbo.messages.incoming.trading.TradeOfferMultipleItemsEvent;
import com.eu.habbo.messages.incoming.trading.TradeStartEvent;
import com.eu.habbo.messages.incoming.trading.TradeUnAcceptEvent;
import com.eu.habbo.messages.incoming.unknown.RequestResolutionEvent;
import com.eu.habbo.messages.incoming.unknown.UnknownEvent1;
import com.eu.habbo.messages.incoming.users.ActivateEffectEvent;
import com.eu.habbo.messages.incoming.users.ChangeChatBubbleEvent;
import com.eu.habbo.messages.incoming.users.ChangeNameCheckUsernameEvent;
import com.eu.habbo.messages.incoming.users.ConfirmChangeNameEvent;
import com.eu.habbo.messages.incoming.users.EnableEffectEvent;
import com.eu.habbo.messages.incoming.users.PickNewUserGiftEvent;
import com.eu.habbo.messages.incoming.users.RequestClubCenterEvent;
import com.eu.habbo.messages.incoming.users.RequestMeMenuSettingsEvent;
import com.eu.habbo.messages.incoming.users.RequestProfileFriendsEvent;
import com.eu.habbo.messages.incoming.users.RequestUserCitizinShipEvent;
import com.eu.habbo.messages.incoming.users.RequestUserClubEvent;
import com.eu.habbo.messages.incoming.users.RequestUserCreditsEvent;
import com.eu.habbo.messages.incoming.users.RequestUserDataEvent;
import com.eu.habbo.messages.incoming.users.RequestUserProfileEvent;
import com.eu.habbo.messages.incoming.users.RequestUserWardrobeEvent;
import com.eu.habbo.messages.incoming.users.RequestWearingBadgesEvent;
import com.eu.habbo.messages.incoming.users.SaveBlockCameraFollowEvent;
import com.eu.habbo.messages.incoming.users.SaveIgnoreRoomInvitesEvent;
import com.eu.habbo.messages.incoming.users.SaveMottoEvent;
import com.eu.habbo.messages.incoming.users.SavePreferOldChatEvent;
import com.eu.habbo.messages.incoming.users.SaveUserVolumesEvent;
import com.eu.habbo.messages.incoming.users.SaveWardrobeEvent;
import com.eu.habbo.messages.incoming.users.UpdateUIFlagsEvent;
import com.eu.habbo.messages.incoming.users.UserActivityEvent;
import com.eu.habbo.messages.incoming.users.UserNuxEvent;
import com.eu.habbo.messages.incoming.users.UserSaveLookEvent;
import com.eu.habbo.messages.incoming.users.UserWearBadgeEvent;
import com.eu.habbo.messages.incoming.wired.WiredApplySetConditionsEvent;
import com.eu.habbo.messages.incoming.wired.WiredConditionSaveDataEvent;
import com.eu.habbo.messages.incoming.wired.WiredEffectSaveDataEvent;
import com.eu.habbo.messages.incoming.wired.WiredTriggerSaveDataEvent;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.emulator.EmulatorConfigUpdatedEvent;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/PacketManager.class */
public class PacketManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketManager.class);
    private static final List<Integer> logList = new ArrayList();
    public static boolean DEBUG_SHOW_PACKETS = false;
    public static boolean MULTI_THREADED_PACKET_HANDLING = false;
    private final THashMap<Integer, Class<? extends MessageHandler>> incoming = new THashMap<>();
    private final THashMap<Integer, List<ICallable>> callables = new THashMap<>();
    private final PacketNames names = new PacketNames();

    public PacketManager() throws Exception {
        this.names.initialize();
        registerHandshake();
        registerCatalog();
        registerEvent();
        registerFriends();
        registerNavigator();
        registerUsers();
        registerHotelview();
        registerInventory();
        registerRooms();
        registerPolls();
        registerUnknown();
        registerModTool();
        registerTrading();
        registerGuilds();
        registerPets();
        registerWired();
        registerAchievements();
        registerFloorPlanEditor();
        registerAmbassadors();
        registerGuides();
        registerCrafting();
        registerCamera();
        registerGameCenter();
    }

    public PacketNames getNames() {
        return this.names;
    }

    @EventHandler
    public static void onConfigurationUpdated(EmulatorConfigUpdatedEvent emulatorConfigUpdatedEvent) {
        logList.clear();
        for (String str : Emulator.getConfig().getValue("debug.show.headers").split(";")) {
            try {
                logList.add(Integer.valueOf(str));
            } catch (NumberFormatException e) {
            }
        }
    }

    public void registerHandler(Integer num, Class<? extends MessageHandler> cls) throws Exception {
        if (num.intValue() < 0) {
            return;
        }
        if (this.incoming.containsKey(num)) {
            throw new Exception("Header already registered. Failed to register " + cls.getName() + " with header " + num);
        }
        this.incoming.putIfAbsent(num, cls);
    }

    public void registerCallable(Integer num, ICallable iCallable) {
        this.callables.putIfAbsent(num, new ArrayList());
        ((List) this.callables.get(num)).add(iCallable);
    }

    public void unregisterCallables(Integer num, ICallable iCallable) {
        if (this.callables.containsKey(num)) {
            ((List) this.callables.get(num)).remove(iCallable);
        }
    }

    public void unregisterCallables(Integer num) {
        if (this.callables.containsKey(num)) {
            this.callables.clear();
        }
    }

    public void handlePacket(GameClient gameClient, ClientMessage clientMessage) {
        if (gameClient == null || Emulator.isShuttingDown) {
            return;
        }
        try {
            if (isRegistered(clientMessage.getMessageId())) {
                Class<? extends MessageHandler> cls = (Class) this.incoming.get(Integer.valueOf(clientMessage.getMessageId()));
                if (cls == null) {
                    throw new Exception("Unknown message " + clientMessage.getMessageId());
                }
                if (gameClient.getHabbo() == null && !cls.isAnnotationPresent(NoAuthMessage.class)) {
                    if (DEBUG_SHOW_PACKETS) {
                        LOGGER.warn("Client packet {} requires an authenticated session.", Integer.valueOf(clientMessage.getMessageId()));
                        return;
                    }
                    return;
                }
                MessageHandler messageHandler = (MessageHandler) cls.newInstance();
                if (messageHandler.getRatelimit() > 0) {
                    if (gameClient.messageTimestamps.containsKey(cls) && System.currentTimeMillis() - gameClient.messageTimestamps.get(cls).longValue() < messageHandler.getRatelimit()) {
                        if (DEBUG_SHOW_PACKETS) {
                            LOGGER.warn("Client packet {} was ratelimited.", Integer.valueOf(clientMessage.getMessageId()));
                            return;
                        }
                        return;
                    }
                    gameClient.messageTimestamps.put(cls, Long.valueOf(System.currentTimeMillis()));
                }
                if (logList.contains(Integer.valueOf(clientMessage.getMessageId())) && gameClient.getHabbo() != null) {
                    LOGGER.info("User {} sent packet {} with body {}", new Object[]{gameClient.getHabbo().getHabboInfo().getUsername(), Integer.valueOf(clientMessage.getMessageId()), clientMessage.getMessageBody()});
                }
                messageHandler.client = gameClient;
                messageHandler.packet = clientMessage;
                if (this.callables.containsKey(Integer.valueOf(clientMessage.getMessageId()))) {
                    Iterator it = ((List) this.callables.get(Integer.valueOf(clientMessage.getMessageId()))).iterator();
                    while (it.hasNext()) {
                        ((ICallable) it.next()).call(messageHandler);
                    }
                }
                if (!messageHandler.isCancelled) {
                    messageHandler.handle();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    boolean isRegistered(int i) {
        return this.incoming.containsKey(Integer.valueOf(i));
    }

    private void registerAmbassadors() throws Exception {
        registerHandler(Integer.valueOf(Incoming.AmbassadorAlertCommandEvent), AmbassadorAlertCommandEvent.class);
        registerHandler(Integer.valueOf(Incoming.AmbassadorVisitCommandEvent), AmbassadorVisitCommandEvent.class);
    }

    private void registerCatalog() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestRecylerLogicEvent), RequestRecyclerLogicEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestDiscountEvent), RequestDiscountEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGiftConfigurationEvent), RequestGiftConfigurationEvent.class);
        registerHandler(Integer.valueOf(Incoming.GetMarketplaceConfigEvent), RequestMarketplaceConfigEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCatalogModeEvent), RequestCatalogModeEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCatalogIndexEvent), RequestCatalogIndexEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCatalogPageEvent), RequestCatalogPageEvent.class);
        registerHandler(1411, CatalogBuyItemAsGiftEvent.class);
        registerHandler(Integer.valueOf(Incoming.CatalogBuyItemEvent), CatalogBuyItemEvent.class);
        registerHandler(339, RedeemVoucherEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReloadRecyclerEvent), ReloadRecyclerEvent.class);
        registerHandler(2771, RecycleEvent.class);
        registerHandler(Integer.valueOf(Incoming.OpenRecycleBoxEvent), OpenRecycleBoxEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestOwnItemsEvent), RequestOwnItemsEvent.class);
        registerHandler(Integer.valueOf(Incoming.TakeBackItemEvent), TakeBackItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestOffersEvent), RequestOffersEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestItemInfoEvent), RequestItemInfoEvent.class);
        registerHandler(Integer.valueOf(Incoming.BuyItemEvent), BuyItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestSellItemEvent), RequestSellItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.SellItemEvent), SellItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCreditsEvent), RequestCreditsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestPetBreedsEvent), RequestPetBreedsEvent.class);
        registerHandler(Integer.valueOf(Incoming.CheckPetNameEvent), CheckPetNameEvent.class);
        registerHandler(3285, RequestClubDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestClubGiftsEvent), RequestClubGiftsEvent.class);
        registerHandler(Integer.valueOf(Incoming.CatalogSearchedItemEvent), CatalogSearchedItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.PurchaseTargetOfferEvent), PurchaseTargetOfferEvent.class);
        registerHandler(Integer.valueOf(Incoming.TargetOfferStateEvent), TargetOfferStateEvent.class);
        registerHandler(Integer.valueOf(Incoming.CatalogSelectClubGiftEvent), CatalogSelectClubGiftEvent.class);
        registerHandler(869, RequestClubCenterEvent.class);
        registerHandler(Integer.valueOf(Incoming.CatalogRequestClubDiscountEvent), CatalogRequestClubDiscountEvent.class);
        registerHandler(Integer.valueOf(Incoming.CatalogBuyClubDiscountEvent), CatalogBuyClubDiscountEvent.class);
    }

    private void registerEvent() throws Exception {
        registerHandler(Integer.valueOf(Incoming.AdventCalendarOpenDayEvent), AdventCalendarOpenDayEvent.class);
        registerHandler(Integer.valueOf(Incoming.AdventCalendarForceOpenEvent), AdventCalendarForceOpenEvent.class);
    }

    private void registerHandshake() throws Exception {
        registerHandler(4000, ReleaseVersionEvent.class);
        registerHandler(Integer.valueOf(Incoming.InitDiffieHandshake), InitDiffieHandshakeEvent.class);
        registerHandler(Integer.valueOf(Incoming.CompleteDiffieHandshake), CompleteDiffieHandshakeEvent.class);
        registerHandler(Integer.valueOf(Incoming.SecureLoginEvent), SecureLoginEvent.class);
        registerHandler(Integer.valueOf(Incoming.MachineIDEvent), MachineIDEvent.class);
        registerHandler(3878, UsernameEvent.class);
        registerHandler(Integer.valueOf(Incoming.PingEvent), PingEvent.class);
    }

    private void registerFriends() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestFriendsEvent), RequestFriendsEvent.class);
        registerHandler(Integer.valueOf(Incoming.ChangeRelationEvent), ChangeRelationEvent.class);
        registerHandler(1689, RemoveFriendEvent.class);
        registerHandler(1210, SearchUserEvent.class);
        registerHandler(Integer.valueOf(Incoming.FriendRequestEvent), FriendRequestEvent.class);
        registerHandler(Integer.valueOf(Incoming.AcceptFriendRequest), AcceptFriendRequestEvent.class);
        registerHandler(2890, DeclineFriendRequestEvent.class);
        registerHandler(Integer.valueOf(Incoming.FriendPrivateMessageEvent), FriendPrivateMessageEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestFriendRequestEvent), RequestFriendRequestsEvent.class);
        registerHandler(Integer.valueOf(Incoming.StalkFriendEvent), StalkFriendEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestInitFriendsEvent), RequestInitFriendsEvent.class);
        registerHandler(Integer.valueOf(Incoming.FindNewFriendsEvent), FindNewFriendsEvent.class);
        registerHandler(Integer.valueOf(Incoming.InviteFriendsEvent), InviteFriendsEvent.class);
    }

    private void registerUsers() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestUserDataEvent), RequestUserDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestUserCreditsEvent), RequestUserCreditsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestUserClubEvent), RequestUserClubEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestMeMenuSettingsEvent), RequestMeMenuSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestUserCitizinShipEvent), RequestUserCitizinShipEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestUserProfileEvent), RequestUserProfileEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestProfileFriendsEvent), RequestProfileFriendsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestUserWardrobeEvent), RequestUserWardrobeEvent.class);
        registerHandler(800, SaveWardrobeEvent.class);
        registerHandler(2228, SaveMottoEvent.class);
        registerHandler(Integer.valueOf(Incoming.UserSaveLookEvent), UserSaveLookEvent.class);
        registerHandler(Integer.valueOf(Incoming.UserWearBadgeEvent), UserWearBadgeEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestWearingBadgesEvent), RequestWearingBadgesEvent.class);
        registerHandler(Integer.valueOf(Incoming.SaveUserVolumesEvent), SaveUserVolumesEvent.class);
        registerHandler(Integer.valueOf(Incoming.SaveBlockCameraFollowEvent), SaveBlockCameraFollowEvent.class);
        registerHandler(Integer.valueOf(Incoming.SaveIgnoreRoomInvitesEvent), SaveIgnoreRoomInvitesEvent.class);
        registerHandler(Integer.valueOf(Incoming.SavePreferOldChatEvent), SavePreferOldChatEvent.class);
        registerHandler(Integer.valueOf(Incoming.ActivateEffectEvent), ActivateEffectEvent.class);
        registerHandler(1752, EnableEffectEvent.class);
        registerHandler(Integer.valueOf(Incoming.UserActivityEvent), UserActivityEvent.class);
        registerHandler(Integer.valueOf(Incoming.UserNuxEvent), UserNuxEvent.class);
        registerHandler(Integer.valueOf(Incoming.PickNewUserGiftEvent), PickNewUserGiftEvent.class);
        registerHandler(Integer.valueOf(Incoming.ChangeNameCheckUsernameEvent), ChangeNameCheckUsernameEvent.class);
        registerHandler(Integer.valueOf(Incoming.ConfirmChangeNameEvent), ConfirmChangeNameEvent.class);
        registerHandler(Integer.valueOf(Incoming.ChangeChatBubbleEvent), ChangeChatBubbleEvent.class);
        registerHandler(Integer.valueOf(Incoming.UpdateUIFlagsEvent), UpdateUIFlagsEvent.class);
    }

    private void registerNavigator() throws Exception {
        registerHandler(3027, RequestRoomCategoriesEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestPopularRoomsEvent), RequestPopularRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestHighestScoreRoomsEvent), RequestHighestScoreRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestMyRoomsEvent), RequestMyRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCanCreateRoomEvent), RequestCanCreateRoomEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestPromotedRoomsEvent), RequestPromotedRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCreateRoomEvent), RequestCreateRoomEvent.class);
        registerHandler(826, RequestTagsEvent.class);
        registerHandler(-1, SearchRoomsByTagEvent.class);
        registerHandler(Integer.valueOf(Incoming.SearchRoomsEvent), SearchRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.SearchRoomsFriendsNowEvent), SearchRoomsFriendsNowEvent.class);
        registerHandler(Integer.valueOf(Incoming.SearchRoomsFriendsOwnEvent), SearchRoomsFriendsOwnEvent.class);
        registerHandler(Integer.valueOf(Incoming.SearchRoomsWithRightsEvent), SearchRoomsWithRightsEvent.class);
        registerHandler(39, SearchRoomsInGroupEvent.class);
        registerHandler(Integer.valueOf(Incoming.SearchRoomsMyFavoriteEvent), SearchRoomsMyFavouriteEvent.class);
        registerHandler(Integer.valueOf(Incoming.SearchRoomsVisitedEvent), SearchRoomsVisitedEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestNewNavigatorDataEvent), RequestNewNavigatorDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestNewNavigatorRoomsEvent), RequestNewNavigatorRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.NewNavigatorActionEvent), NewNavigatorActionEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestNavigatorSettingsEvent), RequestNavigatorSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.SaveWindowSettingsEvent), SaveWindowSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestDeleteRoomEvent), RequestDeleteRoomEvent.class);
        registerHandler(Integer.valueOf(Incoming.NavigatorCategoryListModeEvent), NavigatorCategoryListModeEvent.class);
        registerHandler(Integer.valueOf(Incoming.NavigatorCollapseCategoryEvent), NavigatorCollapseCategoryEvent.class);
        registerHandler(Integer.valueOf(Incoming.NavigatorUncollapseCategoryEvent), NavigatorUncollapseCategoryEvent.class);
        registerHandler(Integer.valueOf(Incoming.AddSavedSearchEvent), AddSavedSearchEvent.class);
        registerHandler(Integer.valueOf(Incoming.DeleteSavedSearchEvent), DeleteSavedSearchEvent.class);
    }

    private void registerHotelview() throws Exception {
        registerHandler(105, HotelViewEvent.class);
        registerHandler(Integer.valueOf(Incoming.HotelViewRequestBonusRareEvent), HotelViewRequestBonusRareEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestNewsListEvent), RequestNewsListEvent.class);
        registerHandler(Integer.valueOf(Incoming.HotelViewDataEvent), HotelViewDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.HotelViewRequestBadgeRewardEvent), HotelViewRequestBadgeRewardEvent.class);
        registerHandler(-1, HotelViewClaimBadgeRewardEvent.class);
        registerHandler(Integer.valueOf(Incoming.HotelViewRequestLTDAvailabilityEvent), HotelViewRequestLTDAvailabilityEvent.class);
        registerHandler(Integer.valueOf(Incoming.HotelViewRequestSecondsUntilEvent), HotelViewRequestSecondsUntilEvent.class);
    }

    private void registerInventory() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestInventoryBadgesEvent), RequestInventoryBadgesEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestInventoryBotsEvent), RequestInventoryBotsEvent.class);
        registerHandler(3150, RequestInventoryItemsEvent.class);
        registerHandler(Integer.valueOf(Incoming.HotelViewInventoryEvent), RequestInventoryItemsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestInventoryPetsEvent), RequestInventoryPetsEvent.class);
    }

    void registerRooms() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestRoomLoadEvent), RequestRoomLoadEvent.class);
        registerHandler(3898, RequestRoomHeightmapEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestRoomHeightmapEvent), RequestRoomHeightmapEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomVoteEvent), RoomVoteEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestRoomDataEvent), RequestRoomDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomSettingsSaveEvent), RoomSettingsSaveEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomPlaceItemEvent), RoomPlaceItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RotateMoveItemEvent), RotateMoveItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.MoveWallItemEvent), MoveWallItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomPickupItemEvent), RoomPickupItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomPlacePaintEvent), RoomPlacePaintEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserStartTypingEvent), RoomUserStartTypingEvent.class);
        registerHandler(1474, RoomUserStopTypingEvent.class);
        registerHandler(99, ToggleFloorItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.ToggleWallItemEvent), ToggleWallItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomBackgroundEvent), RoomBackgroundEvent.class);
        registerHandler(Integer.valueOf(Incoming.MannequinSaveNameEvent), MannequinSaveNameEvent.class);
        registerHandler(Integer.valueOf(Incoming.MannequinSaveLookEvent), MannequinSaveLookEvent.class);
        registerHandler(Integer.valueOf(Incoming.FootballGateSaveLookEvent), FootballGateSaveLookEvent.class);
        registerHandler(Integer.valueOf(Incoming.AdvertisingSaveEvent), AdvertisingSaveEvent.class);
        registerHandler(3129, RequestRoomSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.MoodLightSettingsEvent), MoodLightSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.MoodLightTurnOnEvent), MoodLightTurnOnEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserDropHandItemEvent), RoomUserDropHandItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserLookAtPoint), RoomUserLookAtPoint.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserTalkEvent), RoomUserTalkEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserShoutEvent), RoomUserShoutEvent.class);
        registerHandler(1543, RoomUserWhisperEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserActionEvent), RoomUserActionEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserSitEvent), RoomUserSitEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserDanceEvent), RoomUserDanceEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserSignEvent), RoomUserSignEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserWalkEvent), RoomUserWalkEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserGiveRespectEvent), RoomUserGiveRespectEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserGiveRightsEvent), RoomUserGiveRightsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomRemoveRightsEvent), RoomRemoveRightsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestRoomRightsEvent), RequestRoomRightsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomRemoveAllRightsEvent), RoomRemoveAllRightsEvent.class);
        registerHandler(2064, RoomUserRemoveRightsEvent.class);
        registerHandler(Integer.valueOf(Incoming.BotPlaceEvent), BotPlaceEvent.class);
        registerHandler(Integer.valueOf(Incoming.BotPickupEvent), BotPickupEvent.class);
        registerHandler(2624, BotSaveSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.BotSettingsEvent), BotSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.TriggerDiceEvent), TriggerDiceEvent.class);
        registerHandler(1533, CloseDiceEvent.class);
        registerHandler(Integer.valueOf(Incoming.TriggerColorWheelEvent), TriggerColorWheelEvent.class);
        registerHandler(Integer.valueOf(Incoming.RedeemItemEvent), RedeemItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.PetPlaceEvent), PetPlaceEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserKickEvent), RoomUserKickEvent.class);
        registerHandler(Integer.valueOf(Incoming.SetStackHelperHeightEvent), SetStackHelperHeightEvent.class);
        registerHandler(Integer.valueOf(Incoming.TriggerOneWayGateEvent), TriggerOneWayGateEvent.class);
        registerHandler(Integer.valueOf(Incoming.HandleDoorbellEvent), HandleDoorbellEvent.class);
        registerHandler(Integer.valueOf(Incoming.RedeemClothingEvent), RedeemClothingEvent.class);
        registerHandler(Integer.valueOf(Incoming.PostItPlaceEvent), PostItPlaceEvent.class);
        registerHandler(3964, PostItRequestDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.PostItSaveDataEvent), PostItSaveDataEvent.class);
        registerHandler(3336, PostItDeleteEvent.class);
        registerHandler(Integer.valueOf(Incoming.MoodLightSaveSettingsEvent), MoodLightSaveSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RentSpaceEvent), RentSpaceEvent.class);
        registerHandler(Integer.valueOf(Incoming.RentSpaceCancelEvent), RentSpaceCancelEvent.class);
        registerHandler(Integer.valueOf(Incoming.SetHomeRoomEvent), SetHomeRoomEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserGiveHandItemEvent), RoomUserGiveHandItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomMuteEvent), RoomMuteEvent.class);
        registerHandler(1911, RequestRoomWordFilterEvent.class);
        registerHandler(3001, RoomWordFilterModifyEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomStaffPickEvent), RoomStaffPickEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomRequestBannedUsersEvent), RoomRequestBannedUsersEvent.class);
        registerHandler(Integer.valueOf(Incoming.JukeBoxRequestTrackCodeEvent), JukeBoxRequestTrackCodeEvent.class);
        registerHandler(3082, JukeBoxRequestTrackDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.JukeBoxAddSoundTrackEvent), JukeBoxAddSoundTrackEvent.class);
        registerHandler(Integer.valueOf(Incoming.JukeBoxRemoveSoundTrackEvent), JukeBoxRemoveSoundTrackEvent.class);
        registerHandler(Integer.valueOf(Incoming.JukeBoxRequestPlayListEvent), JukeBoxRequestPlayListEvent.class);
        registerHandler(Integer.valueOf(Incoming.JukeBoxEventOne), JukeBoxEventOne.class);
        registerHandler(1435, JukeBoxEventTwo.class);
        registerHandler(Integer.valueOf(Incoming.SavePostItStickyPoleEvent), SavePostItStickyPoleEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestPromotionRoomsEvent), RequestPromotionRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.BuyRoomPromotionEvent), BuyRoomPromotionEvent.class);
        registerHandler(Integer.valueOf(Incoming.EditRoomPromotionMessageEvent), UpdateRoomPromotionEvent.class);
        registerHandler(Integer.valueOf(Incoming.IgnoreRoomUserEvent), IgnoreRoomUserEvent.class);
        registerHandler(Integer.valueOf(Incoming.UnIgnoreRoomUserEvent), UnIgnoreRoomUserEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUserMuteEvent), RoomUserMuteEvent.class);
        registerHandler(1477, RoomUserBanEvent.class);
        registerHandler(Integer.valueOf(Incoming.UnbanRoomUserEvent), UnbanRoomUserEvent.class);
        registerHandler(17, RequestRoomUserTagsEvent.class);
        registerHandler(Integer.valueOf(Incoming.YoutubeRequestPlaylists), YoutubeRequestPlaylists.class);
        registerHandler(3005, YoutubeRequestStateChange.class);
        registerHandler(Integer.valueOf(Incoming.YoutubeRequestPlaylistChange), YoutubeRequestPlaylistChange.class);
        registerHandler(Integer.valueOf(Incoming.RoomFavoriteEvent), RoomFavoriteEvent.class);
        registerHandler(Integer.valueOf(Incoming.LoveLockStartConfirmEvent), LoveLockStartConfirmEvent.class);
        registerHandler(Integer.valueOf(Incoming.RoomUnFavoriteEvent), RoomUnFavoriteEvent.class);
        registerHandler(Integer.valueOf(Incoming.UseRandomStateItemEvent), UseRandomStateItemEvent.class);
    }

    void registerPolls() throws Exception {
        registerHandler(Integer.valueOf(Incoming.CancelPollEvent), CancelPollEvent.class);
        registerHandler(Integer.valueOf(Incoming.GetPollDataEvent), GetPollDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.AnswerPollEvent), AnswerPollEvent.class);
    }

    void registerModTool() throws Exception {
        registerHandler(Integer.valueOf(Incoming.ModToolRequestRoomInfoEvent), ModToolRequestRoomInfoEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolRequestRoomChatlogEvent), ModToolRequestRoomChatlogEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolRequestUserInfoEvent), ModToolRequestUserInfoEvent.class);
        registerHandler(15, ModToolPickTicketEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolCloseTicketEvent), ModToolCloseTicketEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolReleaseTicketEvent), ModToolReleaseTicketEvent.class);
        registerHandler(1840, ModToolAlertEvent.class);
        registerHandler(-1, ModToolWarnEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolKickEvent), ModToolKickEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolRoomAlertEvent), ModToolRoomAlertEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolChangeRoomSettingsEvent), ModToolChangeRoomSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolRequestRoomVisitsEvent), ModToolRequestRoomVisitsEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolRequestIssueChatlogEvent), ModToolRequestIssueChatlogEvent.class);
        registerHandler(-1, ModToolRequestRoomUserChatlogEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolRequestUserChatlogEvent), ModToolRequestUserChatlogEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolSanctionAlertEvent), ModToolSanctionAlertEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolSanctionMuteEvent), ModToolSanctionMuteEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolSanctionBanEvent), ModToolSanctionBanEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolSanctionTradeLockEvent), ModToolSanctionTradeLockEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolIssueChangeTopicEvent), ModToolIssueChangeTopicEvent.class);
        registerHandler(Integer.valueOf(Incoming.ModToolIssueDefaultSanctionEvent), ModToolIssueDefaultSanctionEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestReportRoomEvent), RequestReportRoomEvent.class);
        registerHandler(3786, RequestReportUserBullyingEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReportBullyEvent), ReportBullyEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReportEvent), ReportEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReportFriendPrivateChatEvent), ReportFriendPrivateChatEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReportThreadEvent), ReportThreadEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReportCommentEvent), ReportCommentEvent.class);
        registerHandler(Integer.valueOf(Incoming.ReportPhotoEvent), ReportPhotoEvent.class);
    }

    void registerTrading() throws Exception {
        registerHandler(Integer.valueOf(Incoming.TradeStartEvent), TradeStartEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeOfferItemEvent), TradeOfferItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeOfferMultipleItemsEvent), TradeOfferMultipleItemsEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeCancelOfferItemEvent), TradeCancelOfferItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeAcceptEvent), TradeAcceptEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeUnAcceptEvent), TradeUnAcceptEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeConfirmEvent), TradeConfirmEvent.class);
        registerHandler(2551, TradeCloseEvent.class);
        registerHandler(Integer.valueOf(Incoming.TradeCancelEvent), TradeCancelEvent.class);
    }

    void registerGuilds() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestGuildBuyRoomsEvent), RequestGuildBuyRoomsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGuildPartsEvent), RequestGuildPartsEvent.class);
        registerHandler(230, RequestGuildBuyEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGuildInfoEvent), RequestGuildInfoEvent.class);
        registerHandler(1004, RequestGuildManageEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGuildMembersEvent), RequestGuildMembersEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGuildJoinEvent), RequestGuildJoinEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildChangeNameDescEvent), GuildChangeNameDescEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildChangeBadgeEvent), GuildChangeBadgeEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildChangeColorsEvent), GuildChangeColorsEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildRemoveAdminEvent), GuildRemoveAdminEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildRemoveMemberEvent), GuildRemoveMemberEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildChangeSettingsEvent), GuildChangeSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildAcceptMembershipEvent), GuildAcceptMembershipEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildDeclineMembershipEvent), GuildDeclineMembershipEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildSetAdminEvent), GuildSetAdminEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildSetFavoriteEvent), GuildSetFavoriteEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestOwnGuildsEvent), RequestOwnGuildsEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGuildFurniWidgetEvent), RequestGuildFurniWidgetEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildConfirmRemoveMemberEvent), GuildConfirmRemoveMemberEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildRemoveFavoriteEvent), GuildRemoveFavoriteEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildDeleteEvent), GuildDeleteEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumListEvent), GuildForumListEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumThreadsEvent), GuildForumThreadsEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumDataEvent), GuildForumDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumPostThreadEvent), GuildForumPostThreadEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumUpdateSettingsEvent), GuildForumUpdateSettingsEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumThreadsMessagesEvent), GuildForumThreadsMessagesEvent.class);
        registerHandler(286, GuildForumModerateMessageEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumModerateThreadEvent), GuildForumModerateThreadEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuildForumThreadUpdateEvent), GuildForumThreadUpdateEvent.class);
        registerHandler(21, GetHabboGuildBadgesMessageEvent.class);
    }

    void registerPets() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestPetInformationEvent), RequestPetInformationEvent.class);
        registerHandler(Integer.valueOf(Incoming.PetPickupEvent), PetPickupEvent.class);
        registerHandler(Integer.valueOf(Incoming.ScratchPetEvent), ScratchPetEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestPetTrainingPanelEvent), RequestPetTrainingPanelEvent.class);
        registerHandler(Integer.valueOf(Incoming.PetUseItemEvent), PetUseItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.HorseRideSettingsEvent), PetRideSettingsEvent.class);
        registerHandler(1036, PetRideEvent.class);
        registerHandler(Integer.valueOf(Incoming.HorseRemoveSaddleEvent), HorseRemoveSaddleEvent.class);
        registerHandler(3379, ToggleMonsterplantBreedableEvent.class);
        registerHandler(Integer.valueOf(Incoming.CompostMonsterplantEvent), CompostMonsterplantEvent.class);
        registerHandler(Integer.valueOf(Incoming.BreedMonsterplantsEvent), BreedMonsterplantsEvent.class);
        registerHandler(Integer.valueOf(Incoming.MovePetEvent), MovePetEvent.class);
        registerHandler(Integer.valueOf(Incoming.PetPackageNameEvent), PetPackageNameEvent.class);
        registerHandler(Integer.valueOf(Incoming.StopBreedingEvent), StopBreedingEvent.class);
        registerHandler(Integer.valueOf(Incoming.ConfirmPetBreedingEvent), ConfirmPetBreedingEvent.class);
    }

    void registerWired() throws Exception {
        registerHandler(Integer.valueOf(Incoming.WiredTriggerSaveDataEvent), WiredTriggerSaveDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.WiredEffectSaveDataEvent), WiredEffectSaveDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.WiredConditionSaveDataEvent), WiredConditionSaveDataEvent.class);
        registerHandler(Integer.valueOf(Incoming.WiredApplySetConditionsEvent), WiredApplySetConditionsEvent.class);
    }

    void registerUnknown() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestResolutionEvent), RequestResolutionEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestTalenTrackEvent), RequestTalentTrackEvent.class);
        registerHandler(Integer.valueOf(Incoming.UnknownEvent1), UnknownEvent1.class);
        registerHandler(Integer.valueOf(Incoming.MySanctionStatusEvent), MySanctionStatusEvent.class);
    }

    void registerFloorPlanEditor() throws Exception {
        registerHandler(Integer.valueOf(Incoming.FloorPlanEditorSaveEvent), FloorPlanEditorSaveEvent.class);
        registerHandler(Integer.valueOf(Incoming.FloorPlanEditorRequestBlockedTilesEvent), FloorPlanEditorRequestBlockedTilesEvent.class);
        registerHandler(3559, FloorPlanEditorRequestDoorSettingsEvent.class);
    }

    void registerAchievements() throws Exception {
        registerHandler(219, RequestAchievementsEvent.class);
        registerHandler(-1, RequestAchievementConfigurationEvent.class);
    }

    void registerGuides() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestGuideToolEvent), RequestGuideToolEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestGuideAssistanceEvent), RequestGuideAssistanceEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideUserTypingEvent), GuideUserTypingEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideReportHelperEvent), GuideReportHelperEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideRecommendHelperEvent), GuideRecommendHelperEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideUserMessageEvent), GuideUserMessageEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideCancelHelpRequestEvent), GuideCancelHelpRequestEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideHandleHelpRequestEvent), GuideHandleHelpRequestEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideInviteUserEvent), GuideInviteUserEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideVisitUserEvent), GuideVisitUserEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuideCloseHelpRequestEvent), GuideCloseHelpRequestEvent.class);
        registerHandler(2501, GuardianNoUpdatesWantedEvent.class);
        registerHandler(3365, GuardianAcceptRequestEvent.class);
        registerHandler(Integer.valueOf(Incoming.GuardianVoteEvent), GuardianVoteEvent.class);
    }

    void registerCrafting() throws Exception {
        registerHandler(Integer.valueOf(Incoming.RequestCraftingRecipesEvent), RequestCraftingRecipesEvent.class);
        registerHandler(Integer.valueOf(Incoming.CraftingAddRecipeEvent), CraftingAddRecipeEvent.class);
        registerHandler(Integer.valueOf(Incoming.CraftingCraftItemEvent), CraftingCraftItemEvent.class);
        registerHandler(Integer.valueOf(Incoming.CraftingCraftSecretEvent), CraftingCraftSecretEvent.class);
        registerHandler(3086, RequestCraftingRecipesAvailableEvent.class);
    }

    void registerCamera() throws Exception {
        registerHandler(Integer.valueOf(Incoming.CameraRoomPictureEvent), CameraRoomPictureEvent.class);
        registerHandler(Integer.valueOf(Incoming.RequestCameraConfigurationEvent), RequestCameraConfigurationEvent.class);
        registerHandler(Integer.valueOf(Incoming.CameraPurchaseEvent), CameraPurchaseEvent.class);
        registerHandler(Integer.valueOf(Incoming.CameraRoomThumbnailEvent), CameraRoomThumbnailEvent.class);
        registerHandler(Integer.valueOf(Incoming.CameraPublishToWebEvent), CameraPublishToWebEvent.class);
    }

    void registerGameCenter() throws Exception {
        registerHandler(Integer.valueOf(Incoming.GameCenterRequestGamesEvent), GameCenterRequestGamesEvent.class);
        registerHandler(Integer.valueOf(Incoming.GameCenterRequestAccountStatusEvent), GameCenterRequestAccountStatusEvent.class);
        registerHandler(Integer.valueOf(Incoming.GameCenterJoinGameEvent), GameCenterJoinGameEvent.class);
        registerHandler(Integer.valueOf(Incoming.GameCenterLoadGameEvent), GameCenterLoadGameEvent.class);
        registerHandler(3207, GameCenterLeaveGameEvent.class);
        registerHandler(Integer.valueOf(Incoming.GameCenterEvent), GameCenterEvent.class);
        registerHandler(11, GameCenterRequestGameStatusEvent.class);
    }
}

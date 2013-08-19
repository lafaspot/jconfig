package org.commons.jconfig.config;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.NumberRange;
import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.config.KeyNotFound;
import org.commons.jconfig.datatype.ValueType;


/**
 * This is another example, copied from XMAS webservices code. See
 * /src/test/resources/app4.properties for a example of a config file for this
 * class.
 * 
 * Also at the end of this Config class you can see method getRateLimit, that
 * dynamically invokes the based on a method named passed as an argument.
 * 
 * This demonstrates the flexibility, of calling the config manager event
 * without having to import the config class, or having a reference to it. I
 * don't think this should be the typical use case, since it is slower and does
 * not allow for compile time validation. This technique should be used in the
 * last resource and only if performance getting the config value is not
 * critical.
 * 
 */
@Config(description = "WebService ydod rate limit configuration")
@ConfigResource(name = "app4.json")
public class App4Config {

    private Number mGetMetaData;

    @ConfigGet(description = "GetMetaData threshold limit per user.", type = ValueType.Number, defaultValue = "2002")
    public Number getGetMetaData() {
        return mGetMetaData;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setGetMetaData(final Number value) {
        mGetMetaData = value;
    }

    private Number mGetUserData;

    @ConfigGet(description = "GetUserData threshold limit per user.", type = ValueType.Number, defaultValue = "2002")
    public Number getGetUserData() {
        return mGetUserData;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setGetUserData(final Number value) {
        mGetUserData = value;
    }

    private Number mSetMetaData;

    @ConfigGet(description = "SetMetaData threshold limit per user.", type = ValueType.Number, defaultValue = "6006")
    public Number getSetMetaData() {
        return mSetMetaData;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSetMetaData(final Number value) {
        mSetMetaData = value;
    }

    private Number mSetUserData;

    @ConfigGet(description = "SetUserData threshold limit per user.", type = ValueType.Number, defaultValue = "6006")
    public Number getSetUserData() {
        return mSetUserData;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSetUserData(final Number value) {
        mSetUserData = value;
    }

    private Number mFetchExternalMail;

    @ConfigGet(
            description = "FetchExternalMail threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "30030")
            public Number getFetchExternalMail() {
        return mFetchExternalMail;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setFetchExternalMail(final Number value) {
        mFetchExternalMail = value;
    }

    private Number mGetMessageBodyPart;

    @ConfigGet(
            description = "GetMessageBodyPart threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "2730")
            public Number getGetMessageBodyPart() {
        return mGetMessageBodyPart;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setGetMessageBodyPart(final Number value) {
        mGetMessageBodyPart = value;
    }

    private Number mGetMessageRawHeader;

    @ConfigGet(
            description = "GetMessageRawHeader threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "2730")
            public Number getGetMessageRawHeader() {
        return mGetMessageRawHeader;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setGetMessageRawHeader(final Number value) {
        mGetMessageRawHeader = value;
    }

    private Number mGetMessage;

    @ConfigGet(description = "GetMessage threshold limit per user.", type = ValueType.Number, defaultValue = "2730")
    public Number getGetMessage() {
        return mGetMessage;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setGetMessage(final Number value) {
        mGetMessage = value;
    }

    private Number mGetDisplayMessage;

    @ConfigGet(
            description = "GetDisplayMessage threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "2730")
            public Number getGetDisplayMessage() {
        return mGetDisplayMessage;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setGetDisplayMessage(final Number value) {
        mGetDisplayMessage = value;
    }

    private Number mListFolders;

    @ConfigGet(description = "ListFolders threshold limit per user.", type = ValueType.Number, defaultValue = "1876")
    public Number getListFolders() {
        return mListFolders;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setListFolders(final Number value) {
        mListFolders = value;
    }

    private Number mListMessagesFromIds;

    @ConfigGet(
            description = "ListMessagesFromIds threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "3753")
            public Number getListMessagesFromIds() {
        return mListMessagesFromIds;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setListMessagesFromIds(final Number value) {
        mListMessagesFromIds = value;
    }

    private Number mListMessages;

    @ConfigGet(description = "ListMessages threshold limit per user.", type = ValueType.Number, defaultValue = "3753")
    public Number getListMessages() {
        return mListMessages;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setListMessages(final Number value) {
        mListMessages = value;
    }

    private Number mSearchMessages;

    @ConfigGet(description = "ListMessages threshold limit per user.", type = ValueType.Number, defaultValue = "18768")
    public Number getSearchMessages() {
        return mSearchMessages;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSearchMessages(final Number value) {
        mSearchMessages = value;
    }

    private Number mCreateFolder;

    @ConfigGet(description = "CreateFolder threshold limit per user.", type = ValueType.Number, defaultValue = "5460")
    public Number getCreateFolder() {
        return mCreateFolder;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setCreateFolder(final Number value) {
        mCreateFolder = value;
    }

    private Number mRemoveFolder;

    @ConfigGet(description = "RemoveFolder threshold limit per user.", type = ValueType.Number, defaultValue = "5460")
    public Number getRemoveFolder() {
        return mRemoveFolder;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setRemoveFolder(final Number value) {
        mRemoveFolder = value;
    }

    private Number mDeleteMessages;

    @ConfigGet(description = "DeleteMessages threshold limit per user.", type = ValueType.Number, defaultValue = "910")
    public Number getDeleteMessages() {
        return mDeleteMessages;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setDeleteMessages(final Number value) {
        mDeleteMessages = value;
    }

    private Number mEmptyFolder;

    @ConfigGet(description = "EmptyFolder threshold limit per user.", type = ValueType.Number, defaultValue = "5460")
    public Number getEmptyFolder() {
        return mEmptyFolder;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setEmptyFolder(final Number value) {
        mEmptyFolder = value;
    }

    private Number mMoveMessages;

    @ConfigGet(description = "MoveMessages threshold limit per user.", type = ValueType.Number, defaultValue = "910")
    public Number getMoveMessages() {
        return mMoveMessages;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setMoveMessages(final Number value) {
        mMoveMessages = value;
    }

    private Number mRenameFolder;

    @ConfigGet(description = "RenameFolder threshold limit per user.", type = ValueType.Number, defaultValue = "5460")
    public Number getRenameFolder() {
        return mRenameFolder;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setRenameFolder(final Number value) {
        mRenameFolder = value;
    }

    private Number mResetSpamFilter;

    @ConfigGet(description = "ResetSpamFilter threshold limit per user.", type = ValueType.Number, defaultValue = "910")
    public Number getResetSpamFilter() {
        return mResetSpamFilter;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setResetSpamFilter(final Number value) {
        mResetSpamFilter = value;
    }

    private Number mSetMessageFlag;

    @ConfigGet(description = "SetMessageFlag threshold limit per user.", type = ValueType.Number, defaultValue = "910")
    public Number getSetMessageFlag() {
        return mSetMessageFlag;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSetMessageFlag(final Number value) {
        mSetMessageFlag = value;
    }

    private Number mVirusScanAttachments;

    @ConfigGet(
            description = "VirusScanAttachments threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "910")
            public Number getVirusScanAttachments() {
        return mVirusScanAttachments;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setVirusScanAttachments(final Number value) {
        mVirusScanAttachments = value;
    }

    private Number mSaveMessage;

    @ConfigGet(description = "SaveMessage threshold limit per user.", type = ValueType.Number, defaultValue = "10010")
    public Number getSaveMessage() {
        return mSaveMessage;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSaveMessage(final Number value) {
        mSaveMessage = value;
    }

    private Number mSaveRawMessage;

    @ConfigGet(
            description = "SaveRawMessage threshold limit per user.",
            type = ValueType.Number,
            defaultValue = "10010")
            public Number getSaveRawMessage() {
        return mSaveRawMessage;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSaveRawMessage(final Number value) {
        mSaveRawMessage = value;
    }

    private Number mSendMessage;

    @ConfigGet(description = "SendMessage threshold limit per user.", type = ValueType.Number, defaultValue = "15015")
    public Number getSendMessage() {
        return mSendMessage;
    }

    @ConfigSet
    @NumberRange(min = 0, max = 65000)
    public void setSendMessage(final Number value) {
        mSendMessage = value;
    }

    /**
     * 
     * @param method
     * @return
     */
    public Number getRateLimit(final String method) {
        try {
            return ConfigManager.INSTANCE.getValueAsNumber(this.getClass(), method);
        } catch (KeyNotFound e) {
            return 0;
        }
    }
}

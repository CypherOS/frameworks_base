package com.android.systemui.smartspace.nano;

import com.android.systemui.R;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;
import java.util.Arrays;

public interface SmartspaceProto {

    public static final class CardWrapper extends MessageNano {
        public SmartspaceCard mCard;
        public long mGsaUpdateTime;
        public int mGsaVersionCode;
        public byte[] mIcon;
        public boolean mIsIconGrayscale;
        public long mPublishTime;

        public CardWrapper() {
            clear();
        }

        public CardWrapper clear() {
            mCard = null;
            mPublishTime = 0;
            mGsaUpdateTime = 0;
            mGsaVersionCode = 0;
            mIcon = WireFormatNano.EMPTY_BYTES;
            mIsIconGrayscale = false;
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (mCard != null) {
                output.writeMessage(1, mCard);
            }
            if (mPublishTime != 0) {
                output.writeInt64(2, mPublishTime);
            }
            if (mGsaUpdateTime != 0) {
                output.writeInt64(3, mGsaUpdateTime);
            }
            if (mGsaVersionCode != 0) {
                output.writeInt32(4, mGsaVersionCode);
            }
            if (!Arrays.equals(mIcon, WireFormatNano.EMPTY_BYTES)) {
                output.writeBytes(5, mIcon);
            }
            if (mIsIconGrayscale) {
                output.writeBool(6, mIsIconGrayscale);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (mCard != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, mCard);
            }
            if (mPublishTime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, mPublishTime);
            }
            if (mGsaUpdateTime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, mGsaUpdateTime);
            }
            if (mGsaVersionCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, mGsaVersionCode);
            }
            if (!Arrays.equals(mIcon, WireFormatNano.EMPTY_BYTES)) {
                size += CodedOutputByteBufferNano.computeBytesSize(5, mIcon);
            }
            if (mIsIconGrayscale) {
                return size + CodedOutputByteBufferNano.computeBoolSize(6, mIsIconGrayscale);
            }
            return size;
        }

        public CardWrapper mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    if (mCard == null) {
                        mCard = new SmartspaceCard();
                    }
                    input.readMessage(mCard);
                } else if (tag == 16) {
                    mPublishTime = input.readInt64();
                } else if (tag == 24) {
                    mGsaUpdateTime = input.readInt64();
                } else if (tag == 32) {
                    mGsaVersionCode = input.readInt32();
                } else if (tag == 42) {
                    mIcon = input.readBytes();
                } else if (tag == 48) {
                    mIsIconGrayscale = input.readBool();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }
    }

    public static final class SmartspaceUpdate extends MessageNano {
        public SmartspaceCard[] mCard;

        public static final class SmartspaceCard extends MessageNano {
            private static volatile SmartspaceCard[] _emptyArray;
            public int cardId;
            public int cardPriority;
            public int cardType;
            public Message duringEvent;
            public long eventDurationMillis;
            public long eventTimeMillis;
            public ExpiryCriteria expiryCriteria;
            public Image mIcon;
            public Message postEvent;
            public Message preEvent;
            public boolean shouldDiscard;
            public TapAction tapAction;
            public long updateTimeMillis;

            public static final class ExpiryCriteria extends MessageNano {
                public long expirationTimeMillis;
                public int maxImpressions;

                public ExpiryCriteria() {
                    clear();
                }

                public ExpiryCriteria clear() {
                    expirationTimeMillis = 0;
                    maxImpressions = 0;
                    cachedSize = -1;
                    return this;
                }

                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    if (expirationTimeMillis != 0) {
                        output.writeInt64(1, expirationTimeMillis);
                    }
                    if (maxImpressions != 0) {
                        output.writeInt32(2, maxImpressions);
                    }
                    super.writeTo(output);
                }

                protected int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    if (expirationTimeMillis != 0) {
                        size += CodedOutputByteBufferNano.computeInt64Size(1, expirationTimeMillis);
                    }
                    if (maxImpressions != 0) {
                        return size + CodedOutputByteBufferNano.computeInt32Size(2, maxImpressions);
                    }
                    return size;
                }

                public ExpiryCriteria mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 8) {
                            expirationTimeMillis = input.readInt64();
                        } else if (tag == 16) {
                            maxImpressions = input.readInt32();
                        } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }
            }

            public static final class Image extends MessageNano {
                public String gsaResourceName;
                public String key;
                public String uri;

                public Image() {
                    clear();
                }

                public Image clear() {
                    key = "";
                    gsaResourceName = "";
                    uri = "";
                    cachedSize = -1;
                    return this;
                }

                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    if (!key.equals("")) {
                        output.writeString(1, key);
                    }
                    if (!gsaResourceName.equals("")) {
                        output.writeString(2, gsaResourceName);
                    }
                    if (!uri.equals("")) {
                        output.writeString(3, uri);
                    }
                    super.writeTo(output);
                }

                protected int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    if (!key.equals("")) {
                        size += CodedOutputByteBufferNano.computeStringSize(1, key);
                    }
                    if (!gsaResourceName.equals("")) {
                        size += CodedOutputByteBufferNano.computeStringSize(2, gsaResourceName);
                    }
                    if (uri.equals("")) {
                        return size;
                    }
                    return size + CodedOutputByteBufferNano.computeStringSize(3, uri);
                }

                public Image mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 10) {
                            key = input.readString();
                        } else if (tag == 18) {
                            gsaResourceName = input.readString();
                        } else if (tag == 26) {
                            uri = input.readString();
                        } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }
            }

            public static final class Message extends MessageNano {
                public FormattedText subtitle;
                public FormattedText title;

                public static final class FormattedText extends MessageNano {
                    public FormatParam[] formatParam;
                    public String text;
                    public int truncateLocation;

                    public static final class FormatParam extends MessageNano {
                        private static volatile FormatParam[] _emptyArray;
                        public int formatParamArgs;
                        public String text;
                        public int truncateLocation;
                        public boolean updateTimeLocally;

                        public static FormatParam[] emptyArray() {
                            if (_emptyArray == null) {
                                synchronized (InternalNano.LAZY_INIT_LOCK) {
                                    if (_emptyArray == null) {
                                        _emptyArray = new FormatParam[0];
                                    }
                                }
                            }
                            return _emptyArray;
                        }

                        public FormatParam() {
                            clear();
                        }

                        public FormatParam clear() {
                            text = "";
                            truncateLocation = 0;
                            formatParamArgs = 0;
                            updateTimeLocally = false;
                            cachedSize = -1;
                            return this;
                        }

                        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                            if (!text.equals("")) {
                                output.writeString(1, text);
                            }
                            if (truncateLocation != 0) {
                                output.writeInt32(2, truncateLocation);
                            }
                            if (formatParamArgs != 0) {
                                output.writeInt32(3, formatParamArgs);
                            }
                            if (updateTimeLocally) {
                                output.writeBool(4, updateTimeLocally);
                            }
                            super.writeTo(output);
                        }

                        protected int computeSerializedSize() {
                            int size = super.computeSerializedSize();
                            if (!text.equals("")) {
                                size += CodedOutputByteBufferNano.computeStringSize(1, text);
                            }
                            if (truncateLocation != 0) {
                                size += CodedOutputByteBufferNano.computeInt32Size(2, truncateLocation);
                            }
                            if (formatParamArgs != 0) {
                                size += CodedOutputByteBufferNano.computeInt32Size(3, formatParamArgs);
                            }
                            if (updateTimeLocally) {
                                return size + CodedOutputByteBufferNano.computeBoolSize(4, updateTimeLocally);
                            }
                            return size;
                        }

                        public FormatParam mergeFrom(CodedInputByteBufferNano input) throws IOException {
                            while (true) {
                                int tag = input.readTag();
                                if (tag == 0) {
                                    return this;
                                }
                                if (tag != 10) {
                                    int value;
                                    if (tag != 16) {
                                        if (tag == 24) {
                                            value = input.readInt32();
                                            switch (value) {
                                                case 0:
                                                case 1:
                                                case 2:
                                                case 3:
                                                    formatParamArgs = value;
                                                    break;
                                                default:
                                                    break;
                                            }
                                        } else if (tag == 32) {
                                            updateTimeLocally = input.readBool();
                                        } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                                            return this;
                                        }
                                    } else {
                                        value = input.readInt32();
                                        switch (value) {
                                            case 0:
                                            case 1:
                                            case 2:
                                            case 3:
                                                truncateLocation = value;
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                                text = input.readString();
                            }
                        }
                    }

                    public FormattedText() {
                        clear();
                    }

                    public FormattedText clear() {
                        text = "";
                        truncateLocation = 0;
                        formatParam = FormatParam.emptyArray();
                        cachedSize = -1;
                        return this;
                    }

                    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                        if (!text.equals("")) {
                            output.writeString(1, text);
                        }
                        if (truncateLocation != 0) {
                            output.writeInt32(2, truncateLocation);
                        }
                        if (formatParam != null && formatParam.length > 0) {
                            for (FormatParam element : formatParam) {
                                if (element != null) {
                                    output.writeMessage(3, element);
                                }
                            }
                        }
                        super.writeTo(output);
                    }

                    protected int computeSerializedSize() {
                        int size = super.computeSerializedSize();
                        if (!text.equals("")) {
                            size += CodedOutputByteBufferNano.computeStringSize(1, text);
                        }
                        if (truncateLocation != 0) {
                            size += CodedOutputByteBufferNano.computeInt32Size(2, truncateLocation);
                        }
                        if (formatParam != null && formatParam.length > 0) {
                            for (FormatParam element : formatParam) {
                                if (element != null) {
                                    size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                                }
                            }
                        }
                        return size;
                    }

                    public FormattedText mergeFrom(CodedInputByteBufferNano input) throws IOException {
                        while (true) {
                            int tag = input.readTag();
                            if (tag == 0) {
                                return this;
                            }
                            if (tag != 10) {
                                int value;
                                if (tag == 16) {
                                    value = input.readInt32();
                                    switch (value) {
                                        case 0:
                                        case 1:
                                        case 2:
                                        case 3:
                                            truncateLocation = value;
                                            break;
                                        default:
                                            break;
                                    }
                                } else if (tag == 26) {
                                    value = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                                    int i = formatParam == null ? 0 : formatParam.length;
                                    FormatParam[] newArray = new FormatParam[(i + value)];
                                    if (i != 0) {
                                        System.arraycopy(formatParam, 0, newArray, 0, i);
                                    }
                                    while (i < newArray.length - 1) {
                                        newArray[i] = new FormatParam();
                                        input.readMessage(newArray[i]);
                                        input.readTag();
                                        i++;
                                    }
                                    newArray[i] = new FormatParam();
                                    input.readMessage(newArray[i]);
                                    formatParam = newArray;
                                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                                    return this;
                                }
                            } else {
                                text = input.readString();
                            }
                        }
                    }
                }

                public Message() {
                    clear();
                }

                public Message clear() {
                    title = null;
                    subtitle = null;
                    cachedSize = -1;
                    return this;
                }

                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    if (title != null) {
                        output.writeMessage(1, title);
                    }
                    if (subtitle != null) {
                        output.writeMessage(2, subtitle);
                    }
                    super.writeTo(output);
                }

                protected int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    if (title != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, title);
                    }
                    if (subtitle != null) {
                        return size + CodedOutputByteBufferNano.computeMessageSize(2, subtitle);
                    }
                    return size;
                }

                public Message mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 10) {
                            if (title == null) {
                                title = new FormattedText();
                            }
                            input.readMessage(title);
                        } else if (tag == 18) {
                            if (subtitle == null) {
                                subtitle = new FormattedText();
                            }
                            input.readMessage(subtitle);
                        } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }
            }

            public static final class TapAction extends MessageNano {
                public int actionType;
                public String intent;

                public TapAction() {
                    clear();
                }

                public TapAction clear() {
                    actionType = 0;
                    intent = "";
                    cachedSize = -1;
                    return this;
                }

                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    if (actionType != 0) {
                        output.writeInt32(1, actionType);
                    }
                    if (!intent.equals("")) {
                        output.writeString(2, intent);
                    }
                    super.writeTo(output);
                }

                protected int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    if (actionType != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, actionType);
                    }
                    if (intent.equals("")) {
                        return size;
                    }
                    return size + CodedOutputByteBufferNano.computeStringSize(2, intent);
                }

                public TapAction mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag != 0) {
                            if (tag == 8) {
                                int value = input.readInt32();
                                switch (value) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        actionType = value;
                                        break;
                                    default:
                                        break;
                                }
                            } else if (tag == 18) {
                                intent = input.readString();
                            } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                                return this;
                            }
                        } else {
                            return this;
                        }
                    }
                }
            }

            public static SmartspaceCard[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new SmartspaceCard[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public SmartspaceCard() {
                clear();
            }

            public SmartspaceCard clear() {
                shouldDiscard = false;
                cardPriority = 0;
                cardId = 0;
                preEvent = null;
                duringEvent = null;
                postEvent = null;
                mIcon = null;
                cardType = 0;
                tapAction = null;
                updateTimeMillis = 0;
                eventTimeMillis = 0;
                eventDurationMillis = 0;
                expiryCriteria = null;
                cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (shouldDiscard) {
                    output.writeBool(1, shouldDiscard);
                }
                if (cardId != 0) {
                    output.writeInt32(2, cardId);
                }
                if (preEvent != null) {
                    output.writeMessage(3, preEvent);
                }
                if (duringEvent != null) {
                    output.writeMessage(4, duringEvent);
                }
                if (postEvent != null) {
                    output.writeMessage(5, postEvent);
                }
                if (mIcon != null) {
                    output.writeMessage(6, mIcon);
                }
                if (cardType != 0) {
                    output.writeInt32(7, cardType);
                }
                if (tapAction != null) {
                    output.writeMessage(8, tapAction);
                }
                if (updateTimeMillis != 0) {
                    output.writeInt64(9, updateTimeMillis);
                }
                if (eventTimeMillis != 0) {
                    output.writeInt64(10, eventTimeMillis);
                }
                if (eventDurationMillis != 0) {
                    output.writeInt64(11, eventDurationMillis);
                }
                if (expiryCriteria != null) {
                    output.writeMessage(12, expiryCriteria);
                }
                if (cardPriority != 0) {
                    output.writeInt32(13, cardPriority);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (shouldDiscard) {
                    size += CodedOutputByteBufferNano.computeBoolSize(1, shouldDiscard);
                }
                if (cardId != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, cardId);
                }
                if (preEvent != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, preEvent);
                }
                if (duringEvent != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, duringEvent);
                }
                if (postEvent != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, postEvent);
                }
                if (mIcon != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(6, mIcon);
                }
                if (cardType != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(7, cardType);
                }
                if (tapAction != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(8, tapAction);
                }
                if (updateTimeMillis != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(9, updateTimeMillis);
                }
                if (eventTimeMillis != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(10, eventTimeMillis);
                }
                if (eventDurationMillis != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(11, eventDurationMillis);
                }
                if (expiryCriteria != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(12, expiryCriteria);
                }
                if (cardPriority != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(13, cardPriority);
                }
                return size;
            }

            public SmartspaceCard mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    int value;
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            shouldDiscard = input.readBool();
                            break;
                        case 16:
                            cardId = input.readInt32();
                            break;
                        case 26:
                            if (preEvent == null) {
                                preEvent = new Message();
                            }
                            input.readMessage(preEvent);
                            break;
                        case 34:
                            if (duringEvent == null) {
                                duringEvent = new Message();
                            }
                            input.readMessage(duringEvent);
                            break;
                        case 42:
                            if (postEvent == null) {
                                postEvent = new Message();
                            }
                            input.readMessage(postEvent);
                            break;
                        case 50:
                            if (mIcon == null) {
                                mIcon = new Image();
                            }
                            input.readMessage(mIcon);
                            break;
                        case 56:
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                    cardType = value;
                                    break;
                                default:
                                    break;
                            }
                        case com.android.internal.R.styleable.editTextBackground:
                            if (tapAction == null) {
                                tapAction = new TapAction();
                            }
                            input.readMessage(tapAction);
                            break;
                        case com.android.internal.R.styleable.listDividerAlertDialog:
                            updateTimeMillis = input.readInt64();
                            break;
                        case 80:
                            eventTimeMillis = input.readInt64();
                            break;
                        case com.android.internal.R.styleable.ratingBarStyleSmall:
                            eventDurationMillis = input.readInt64();
                            break;
                        case com.android.internal.R.styleable.textAppearanceListItemSecondary:
                            if (expiryCriteria == null) {
                                expiryCriteria = new ExpiryCriteria();
                            }
                            input.readMessage(expiryCriteria);
                            break;
                        case com.android.internal.R.styleable.textColorAlertDialogListItem:
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                    cardPriority = value;
                                    break;
                                default:
                                    break;
                            }
                        default:
                            if (WireFormatNano.parseUnknownField(input, tag)) {
                                break;
                            }
                            return this;
                    }
                }
            }
        }

        public SmartspaceUpdate() {
            clear();
        }

        public SmartspaceUpdate clear() {
            mCard = SmartspaceCard.emptyArray();
            cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (mCard != null && mCard.length > 0) {
                for (SmartspaceCard element : mCard) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (mCard != null && mCard.length > 0) {
                for (SmartspaceCard element : mCard) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            return size;
        }

        public SmartspaceUpdate mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = mCard == null ? 0 : mCard.length;
                    SmartspaceCard[] newArray = new SmartspaceCard[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(mCard, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new SmartspaceCard();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new SmartspaceCard();
                    input.readMessage(newArray[i]);
                    mCard = newArray;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }
    }
}

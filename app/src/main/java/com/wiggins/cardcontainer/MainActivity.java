package com.wiggins.cardcontainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wiggins.cardcontainer.base.BaseActivity;
import com.wiggins.cardcontainer.utils.ToastUtil;
import com.wiggins.cardcontainer.utils.UIUtils;
import com.wiggins.cardcontainer.widget.CardContainerView;
import com.wiggins.cardcontainer.widget.RoundImageView;
import com.wiggins.cardcontainer.widget.TitleView;

import java.util.Random;

/**
 * @Description 可拖拽式层叠卡片效果
 * @Author 一花一世界
 */
public class MainActivity extends BaseActivity {

    private TitleView titleView;
    private CardContainerView mCardContainerView;
    private int[] imageIcon = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4,
            R.drawable.pic5, R.drawable.pic6, R.drawable.pic7, R.drawable.pic8,
            R.drawable.pic9, R.drawable.pic10, R.drawable.pic11, R.drawable.pic12};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
        addCard(6);
    }

    private void initView() {
        titleView = (TitleView) findViewById(R.id.titleView);
        titleView.setAppTitle(UIUtils.getString(R.string.title));
        titleView.setLeftImageVisibility(View.GONE);
        mCardContainerView = (CardContainerView) findViewById(R.id.card_container);
        mCardContainerView.setLoadSize(3);
    }

    private void setListener() {
        mCardContainerView.setLoadMoreListener(new CardContainerView.LoadMore() {
            @Override
            public void load() {
                addCard(3);
            }
        });
        mCardContainerView.setLeftOrRightListener(new CardContainerView.LeftOrRight() {
            @Override
            public void leftOrRight(boolean left) {
                if (left) {
                    ToastUtil.showText(UIUtils.getString(R.string.like));
                } else {
                    ToastUtil.showText(UIUtils.getString(R.string.dislike));
                }
            }
        });
    }

    private void addCard(int size) {
        for (int i = 0; i < size; i++) {
            mCardContainerView.addView(getCard(getImageIcon(), getNickName(), getSignature()));
        }
    }

    private int getImageIcon() {
        int indexImageIcon = new Random().nextInt(imageIcon.length);
        return imageIcon[indexImageIcon];
    }

    private String getNickName() {
        String[] nickName = UIUtils.getResources().getStringArray(R.array.nickName);
        int indexNickName = new Random().nextInt(nickName.length);
        return nickName[indexNickName];
    }

    private String getSignature() {
        String[] signature = UIUtils.getResources().getStringArray(R.array.signature);
        int indexSignature = new Random().nextInt(signature.length);
        return signature[indexSignature];
    }

    private View getCard(int imageIcon, String nickName, String signature) {
        View card = LayoutInflater.from(this).inflate(R.layout.activity_card, null);
        RoundImageView mIvIcon = (RoundImageView) card.findViewById(R.id.iv_icon);
        TextView mNickName = (TextView) card.findViewById(R.id.tv_nickName);
        TextView mSignature = (TextView) card.findViewById(R.id.tv_signature);
        ImageView mIvLike = (ImageView) card.findViewById(R.id.iv_like);
        ImageView mIvDisLike = (ImageView) card.findViewById(R.id.iv_disLike);
        mIvIcon.setImageResource(imageIcon);
        mNickName.setText(nickName);
        mSignature.setText(signature);
        mIvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardContainerView.removeTopCard(true);
            }
        });
        mIvDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardContainerView.removeTopCard(false);
            }
        });
        return card;
    }
}

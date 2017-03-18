package com.todo.ui.set;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;

import com.github.markzhai.recyclerview.BaseViewAdapter;
import com.github.markzhai.recyclerview.BindingViewHolder;
import com.github.markzhai.recyclerview.SingleTypeAdapter;
import com.loonggg.lib.alarmmanager.clock.SPUtils;
import com.todo.R;
import com.todo.data.bean.Mp3Info;
import com.todo.databinding.ItemRecorderBinding;
import com.todo.utils.DensityUtil;
import com.todo.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyang on 2017/3/17.
 */
public class RecorderFragment extends Fragment {

    private View mView;
    private RecyclerView recyclerView;
    private FloatingActionButton addBtn;
    private SingleTypeAdapter<Mp3Info> mAdapter;

    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private List<String> fileNameList = new ArrayList<>();
    private List<Mp3Info> mp3InfoList = new ArrayList<>();
    private boolean mStartRecording = true;
    private Chronometer mChronometer;

    public static RecorderFragment newInstance() {
        RecorderFragment fragment = new RecorderFragment();
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_recorder, container, false);
        initView();
        initDatas();
        initEventHandelers();
        return mView;
    }


    private void initView() {
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerview);
        addBtn = (FloatingActionButton) mView.findViewById(R.id.add_btn);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SingleTypeAdapter<>(getActivity(), R.layout.item_recorder);
        mAdapter.setPresenter(new ItemPresenter());
        mAdapter.setDecorator(new DemoAdapterDecorator());
        recyclerView.setAdapter(mAdapter);
        mChronometer = (Chronometer) mView.findViewById(R.id.timer);

    }


    private void initDatas() {
        mp3InfoList.clear();
        fileNameList = GetMediaFileName(getActivity().getFilesDir().getAbsolutePath());
        if (fileNameList != null) {
            for (String s : fileNameList) {
                Mp3Info mp3Info = new Mp3Info();
                mp3Info.setTitle(s.substring(0, s.length() - 4));
                mp3Info.setUrl(getActivity().getFilesDir().getAbsolutePath() + "/" + s);
                mp3InfoList.add(mp3Info);
            }
        }
        String type = (String) SPUtils.get(getActivity(), SPUtils.RING_TYPE_KEY, "");
        String url = (String) SPUtils.get(getActivity(), SPUtils.RECORD_NAME_KEY, "");
        if (type != null && type.equals(SPUtils.RECORD_NAME_KEY) && url != null)
            for (Mp3Info mp3Info : mp3InfoList) {
                if (url.equals(mp3Info.getUrl()))
                    mp3Info.setCheck(true);
            }
        mAdapter.set(mp3InfoList);

    }


    private void initEventHandelers() {

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    addBtn.setImageResource(R.mipmap.recorder_pause);
                } else {
                    addBtn.setImageResource(R.mipmap.recorder_start);
                }
                mStartRecording = !mStartRecording;
            }
        });

    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        int position = (int) SPUtils.get(getActivity(), SPUtils.RECORDER_POSITION_KEY, 0);
        String mFileName = getActivity().getFilesDir().getAbsolutePath();
        mFileName += "/TODO录音" + position + ".amr";
        SPUtils.put(getActivity(), SPUtils.RECORDER_POSITION_KEY, position + 1);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            LogUtil.d("startRecording failed");
        }
        mRecorder.start();
        mChronometer.setVisibility(View.VISIBLE);
        //计时器清零
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mChronometer.setVisibility(View.GONE);
        //计时器清零
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        initDatas();
    }


    private void startPlaying(Mp3Info mp3Info) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mp3Info.getUrl());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            LogUtil.d("startPlaying failed");
        }
    }


    public List<String> GetMediaFileName(String fileAbsolutePath) {
        List<String> fileList = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();

        for (File aSubFile : subFile) {
            // 判断是否为文件夹
            if (!aSubFile.isDirectory()) {
                String filename = aSubFile.getName();
                // 判断是否为amr结尾
                if (filename.trim().toLowerCase().endsWith(".amr")) {
                    fileList.add(filename);
                }
            }
        }
        return fileList;
    }


    public class ItemPresenter implements BaseViewAdapter.Presenter {
        public void onItemClick(Mp3Info mp3Info) {
            for (Mp3Info bean : mp3InfoList)
                bean.setCheck(false);
            mp3Info.setCheck(true);
            mAdapter.set(mp3InfoList);
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            startPlaying(mp3Info);
            SPUtils.put(getActivity(), SPUtils.RING_TYPE_KEY, SPUtils.RECORD_NAME_KEY);
            SPUtils.put(getActivity(), SPUtils.RECORD_NAME_KEY, mp3Info.getUrl());
        }
    }


    public class DemoAdapterDecorator implements BaseViewAdapter.Decorator {
        ItemRecorderBinding binding;


        @Override
        public void decorator(BindingViewHolder holder, final int position, int viewType) {
            binding = (ItemRecorderBinding) holder.getBinding();
            binding.recorderItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final CharSequence[] items = {"重命名", "删除"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0) rename(position);
                            else {
                                File file = new File(mp3InfoList.get(position).getUrl());
                                if (file.isFile() && file.exists()) {
                                    file.delete();
                                }
                                mp3InfoList.remove(position);
                                mAdapter = new SingleTypeAdapter<>(getActivity(), R.layout.item_recorder);
                                mAdapter.setPresenter(new ItemPresenter());
                                mAdapter.setDecorator(new DemoAdapterDecorator());
                                recyclerView.setAdapter(mAdapter);
                                mAdapter.set(mp3InfoList);
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                    return false;
                }
            });


        }
    }


    private void rename(final int position) {
        final EditText et = new EditText(getActivity());
        et.setBackgroundColor(getResources().getColor(R.color.transparent));
        et.setPadding(DensityUtil.dp2px(getActivity(), 15), DensityUtil.dp2px(getActivity(), 10)
                , DensityUtil.dp2px(getActivity(), 15), DensityUtil.dp2px(getActivity(), 10));

        new AlertDialog.Builder(getActivity())
                .setTitle("重命名")
                .setView(et)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mp3InfoList.get(position).setTitle(et.getText().toString());
                        File oldFile = new File(mp3InfoList.get(position).getUrl());
//                        String newPath = getActivity().getFilesDir().getAbsolutePath() + et.getText().toString() + ".amr";
                        oldFile.renameTo(new File(getActivity().getFilesDir().getAbsolutePath(), et.getText().toString() + ".amr"));
                        mAdapter.set(mp3InfoList);
                    }
                })
                .setNegativeButton("取消", null)
                .show();

    }


    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

    }

}

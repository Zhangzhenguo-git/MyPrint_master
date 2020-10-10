package com.zzg.myprint_master;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.print.PrintHelper;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

//import com.aspose.words.Document;
//import com.aspose.words.SaveFormat;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zzg.myprint_master.databinding.ActivityMainBinding;

import net.lemonsoft.lemonbubble.LemonBubble;
import net.lemonsoft.lemonbubble.LemonBubbleInfo;
import net.lemonsoft.lemonbubble.LemonBubbleView;
import net.lemonsoft.lemonbubble.interfaces.LemonBubbleMaskOnTouchContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private ActivityMainBinding binding;
    private int con = 0;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        context = MainActivity.this;
        openPermissions();
    }

    private void myClick() {
        binding.btPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPhotoPrint();
            }
        });
        binding.btAssetsPrintPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAssetsPhotoPrint();
            }
        });
        binding.btSDPrintPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSDPhotoPrint();
            }
        });
        binding.btPrintUrlHTML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHTMLPrint(0);
            }
        });
        binding.btPrintHTML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHTMLPrint(1);
            }
        });
        binding.btPrintContainImgHTML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHTMLPrint(2);
            }
        });
        binding.btPrintCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wordFilePath = Environment.getExternalStorageDirectory() + "/测试文件打印1.docx";
                String pdfFilePath = Environment.getExternalStorageDirectory() + "/测试文件打印.pdf";
                doPrint(pdfFilePath);
            }
        });

    }

    /**
     * 打印png转pdf资源文件
     */
    private void doPhotoPrint() {
        PhotoVerPdf photoVerPdf = new PhotoVerPdf();
        if (photoVerPdf.ready()) {
            doPrint(Environment.getExternalStorageDirectory() + "/测试图片打印.pdf");
        } else {
            LemonBubble.showError(MainActivity.this,"转换失败",1000);
        }
    }

    /**
     * 打印assets资源图片
     */
    private void doAssetsPhotoPrint() {
        PrintHelper printHelper = new PrintHelper(context);
        //此属性会自动调整图像的大小，可以更好的把要打印的图像调整到合适的打印区域
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        //此属性会自动等比例调整图像大小，使图像充满整个打印区域，即让图像充满整个纸张
        //缺点是，打印图像的（上下左右边缘会有一部分打印不出来）
        // printHelper.setScaleMode(PrintHelper.SCALE_MODE_FILL);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scan);
        printHelper.printBitmap("TestPrint", bitmap);
    }

    /**
     * 打印sd卡资源图片
     */
    private void doSDPhotoPrint() {
        PrintHelper printHelper = new PrintHelper(context);
        //此属性会自动调整图像的大小，可以更好的把要打印的图像调整到合适的打印区域
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        //此属性会自动等比例调整图像大小，使图像充满整个打印区域，即让图像充满整个纸张
        //缺点是，打印图像的（上下左右边缘会有一部分打印不出来）
        // printHelper.setScaleMode(PrintHelper.SCALE_MODE_FILL);
        File fileSDImg = new File(Environment.getExternalStorageDirectory() + "/测试图片打印.png");

        try {
            FileInputStream inputStream = new FileInputStream(fileSDImg);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            printHelper.printBitmap("TestPrint", bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * HTML打印
     */
    private void doHTMLPrint(int printType) {
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView1, String url) {
                return false;
            }

            //调用打印任务的入口
            //注意，调用打印方法时，一定要先让页面加载完成，否则会出现打印不完整或空白。
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("执行", url);
                //首先创建一个打印管理器对象并实例化
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                //获取打印适配器实例
                PrintDocumentAdapter pDAdapter = webView.createPrintDocumentAdapter();
                //使用名称和适配起来打印名称
                String jobName = getString(R.string.app_name) + "Document";
                printManager.print(jobName, pDAdapter, new PrintAttributes.Builder().build());
//                printListener(printManager);
            }
        });
        // 创建要加载的代码
        //baseUrl:网页地址，data:请求的某段代码，mimeType:加载网页的类型，encode：编码格式，historyUrl：可用历史记录
        //在线打印
//        String htmlUrl="http://43.248.49.204:8080/2020/03/31/MjAwMzMxNjczNzQzNTUw.html";
        String htmlUrl = "https://developer.huawei.com/consumer/cn/";
        //指定html字符串打印
        String htmlDocument = "<html><body><h1>Test Content测试打印，测试打印</h1><p>Testing, testing, testing...测试测试测试测试</p></body></html>";
        if (printType == 0) {
            webView.loadUrl(htmlUrl);
        } else if (printType == 1) {
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else if (printType == 2) {
            //如果希望打印的页面含有图片，那就把要显示的图片放入工程的assets/目录下,
            webView.loadDataWithBaseURL("file:///android_asset/images/ic_launcher.png", htmlDocument, "text/HTML", "UTF-8", null);
        }
    }

    /**
     * 自定义打印
     */
    private void doPrint(String filePath) {
        if (!PDFCheck.check(filePath)) {
            LemonBubble.showError(MainActivity.this, "文件下载失败", 2000);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printManager = (PrintManager) MainActivity.this.getSystemService(Context.PRINT_SERVICE);
            String jobName = getString(R.string.app_name) + "Document";
            try {
                printManager.print(jobName, new MyPrintDocumentAdapter(MainActivity.this, filePath), null);
            } catch (RuntimeException e) {
            } catch (Exception e) {
            }
        } else {
            Toast.makeText(context, "Android 版本过低不支持自定义打印", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建打印适配器
     */
    private class MyPrintDocumentAdapter extends PrintDocumentAdapter {
        private Context mContext;
        private String mFilePath;
        private PrintedPdfDocument mPdfDocument;

        public PdfDocument myPdfDocument;
        public int totalpages = 1;//设置一共打印一张纸

        public MyPrintDocumentAdapter(Context context, String filePath) {
            this.mContext = context;
            this.mFilePath = filePath;
        }

        //当打印进程开始，该方法就将被调用，
        @Override
        public void onStart() {
            super.onStart();
        }

        //当用户改变了打印输出时，比方说页面尺寸，或者页面的方向时，
        // 该函数将被调用。以此会给我们的应用重新计划打印页面的布局，
        // 另外该方法必须返回打印文档包含多少页面。
        @Override
        public void onLayout(PrintAttributes printAttributes,
                             PrintAttributes printAttributes1,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback layoutResultCallback,
                             Bundle bundle) {
//            //使用请求的页属性创建新的pdfdocument
//            mPdfDocument=new PrintedPdfDocument(mContext,printAttributes1);
            // 响应取消请求
            if (cancellationSignal.isCanceled()) {
                layoutResultCallback.onLayoutCancelled();
                return;
            }
            // 将打印信息返回到打印框架
            PrintDocumentInfo info = new PrintDocumentInfo
                    .Builder("name")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            layoutResultCallback.onLayoutFinished(info, true);
        }

        //此函数被调用后，会将打印页面渲染成一个待打印的文件，该函数
        // 可以在onLayout被调用后调用一次或多次
        @Override
        public void onWrite(PageRange[] pageRanges,
                            ParcelFileDescriptor parcelFileDescriptor,
                            CancellationSignal cancellationSignal,
                            WriteResultCallback writeResultCallback) {
            InputStream input = null;
            OutputStream output = null;
            try {
                input = new FileInputStream(mFilePath);
                output = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }
                writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            } catch (FileNotFoundException e) {
            } catch (Exception e) {
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                }
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
            Toast.makeText(mContext, "已准备好打印，点击右上角蓝色图标开始打印", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开权限
     */
    private void openPermissions() {
        final RxPermissions rxPermissions = new RxPermissions(MainActivity.this); // where this is an Activity or Fragment instance
        rxPermissions.requestEachCombined(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).subscribe(new Consumer<Permission>() {
            @Override
            public void accept(Permission permission) throws Exception {
                if (permission.granted) {
                    Log.d("执行", "权限都通过了");
                    myClick();
                } else if (permission.shouldShowRequestPermissionRationale) {
                    Log.d("执行", "至少有一个权限被拒绝了");
                    openPermissions();
                } else {
                    Log.d("执行", "转到设置");
                }
            }
        });
    }
}
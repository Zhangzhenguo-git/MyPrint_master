package com.zzg.myprint_master;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.print.PrinterInfo;
import android.print.pdf.PrintedPdfDocument;
import android.printservice.PrintDocument;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.util.jar.Attributes;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private PrintHelper printHelper;
    private Context context;
    private Button btPrintPhoto;
    private Button bt_PrintHTML;
    private Button bt_PrintCustom;
    private Button bt_BluetoothPrint;

    private WebView mWebView;
    private PrintJob mPrintJob;
    private PrintManager manager;
    private BluetoothManager bluetoothManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=MainActivity.this;
        openPermissions();
        btPrintPhoto=findViewById(R.id.bt_PrintPhoto);
        bt_PrintHTML=findViewById(R.id.bt_PrintHTML);
        bt_PrintCustom=findViewById(R.id.bt_PrintCustom);
        bt_BluetoothPrint=findViewById(R.id.bt_BluetoothPrint);
        myClick();
    }
    private void myClick(){
        btPrintPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPhotoPrint();
            }
        });
        bt_PrintHTML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHTMLPrint();
            }
        });
        bt_PrintCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPrint();
            }
        });
        bt_BluetoothPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this,BLuetoothActivity.class),0);
//                startActivityForResult(new Intent(MainActivity.this,Test.class),0);
            }
        });

    }

    /**
     * 打印图片
     */
    private void doPhotoPrint(){
        PrintHelper printHelper=new PrintHelper(context);
        //此属性会自动调整图像的大小，可以更好的把要打印的图像调整到合适的打印区域
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        //此属性会自动等比例调整图像大小，使图像充满整个打印区域，即让图像充满整个纸张
        //缺点是，打印图像的（上下左右边缘会有一部分打印不出来）
        // printHelper.setScaleMode(PrintHelper.SCALE_MODE_FILL);
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.test_print_img);
        printHelper.printBitmap("TestPrint",bitmap);
    }

    /**
     * HTML打印
     */
    private void doHTMLPrint(){
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView webView1,String url){
                return false;
            }
            //调用打印任务的入口
            //注意，调用打印方法时，一定要先让页面加载完成，否则会出现打印不完整或空白。
            @Override
            public void onPageFinished(WebView view, String url) {
                createWebPrintJob();
                mWebView=null;
            }
        });
        // 创建要加载的代码
        String htmlDocument = "<html><body><h1>Test Content测试打印，测试打印</h1><p>Testing, " +
                "testing, testing...测试测试测试测试</p></body></html>";
        //baseUrl:网页地址，data:请求的某段代码，mimeType:加载网页的类型，encode：编码格式，historyUrl：可用历史记录
        webView.loadDataWithBaseURL(null,htmlDocument,"text/HTML","UTF-8",null);
        //如果希望打印的页面含有图片，那就把要显示的图片放入工程的assets/目录下,
//        webView.loadDataWithBaseURL("file:///android_asset/images/",htmlDocument,"text/HTML","UTF-8",null);
        mWebView=webView;
    }
    private void createWebPrintJob(){
        //首先创建一个打印管理器对象并实例化
        PrintManager printManager= (PrintManager) getSystemService(Context.PRINT_SERVICE);
        //获取打印适配器实例
        PrintDocumentAdapter printDocumentAdapter=mWebView.createPrintDocumentAdapter();
        //使用名称和适配起来打印名称
        String jobName=getString(R.string.app_name)+"Document";
        printManager.print(jobName,printDocumentAdapter,new PrintAttributes.Builder().build());

    }



    /**
     * 自定义打印
     */
    private void doPrint(){
        // Get a PrintManager instance 获取打印驱动对象
        PrintManager printManager= (PrintManager) getSystemService(Context.PRINT_SERVICE);
        // Set job name, which will be displayed in the print queue 设置作业名称，该名称将显示在打印队列中
        String jobName=getString(R.string.app_name)+"Document";
        // Start a print job, passing in a PrintDocumentAdapter implementation 启动打印作业，传入printdocumentadapter实现
        // to handle the generation of a print document处理打印文档的生成
        String filePath= Environment.getExternalStorageDirectory()+"/测试文件打印.pdf";
        Toast.makeText(context, "打印文件路径："+filePath, Toast.LENGTH_SHORT).show();
        printManager.print(jobName,new MyPrintDocumentAdapter(context,filePath),null);
    }

    /**
     * 创建打印适配器
     */
    private class MyPrintDocumentAdapter extends PrintDocumentAdapter {
        private Context mContext;
        private String mFilePath;
        private PrintedPdfDocument mPdfDocument;
        public MyPrintDocumentAdapter(Context context,String filePath){
            this.mContext=context;
            this.mFilePath=filePath;
        }
        //当打印进程开始，该方法就将被调用，
        @Override
        public void onStart() {
            Toast.makeText(mContext, "准备开始", Toast.LENGTH_SHORT).show();
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
            // Create a new PdfDocument with the requested page attributes
            //使用请求的页属性创建新的pdfdocument
            mPdfDocument=new PrintedPdfDocument(mContext,printAttributes1);
            // Respond to cancellation request
            // 响应取消请求
            if (cancellationSignal.isCanceled() ) {
                layoutResultCallback.onLayoutCancelled();
                return;
            }
//            Compute the expected number of printed pages
//            计算预期的打印页数
//            int pages = computePageCount(printAttributes1);
//            if (pages > 0) {
//                // Return print information to print framework
//                // 将打印信息返回到打印框架
//                PrintDocumentInfo info = new PrintDocumentInfo
//                        .Builder("print_output.pdf")
//                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
//                        .setPageCount(pages)
//                        .build();
//                // Content layout reflow is complete
//                // 内容布局回流已完成
//                layoutResultCallback.onLayoutFinished(info, true);
//            } else {
//                // Otherwise report an error to the print framework
//                //否则向打印框架报告错误
//                layoutResultCallback.onLayoutFailed("Page count calculation failed.");
//            }

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
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(mContext, "待打印状态", Toast.LENGTH_SHORT).show();
            // Iterate over each page of the document,
            // 遍历文档的每一页，
            // check if it's in the output range.
            //检查是否在输出范围内
//            for (int i = 0; i <pageRanges.length; i++) {
//                // Check to see if this page is in the output range.
//                // 检查此页是否在输出范围内。
//                if (containsPage(pageRanges, i)) {
//                    // If so, add it to writtenPagesArray. writtenPagesArray.size()
//                    //如果是，请将其添加到writenpagesarray。写入网页array.size（）
//                    // is used to compute the next output page index.
//                    //用于计算下一个输出页索引。
//                    writtenPagesArray.append(writtenPagesArray.size(), i);
//
//                    PdfDocument.Page page = mPdfDocument.startPage(i);
//                    // check for cancellation
//                    // 检查取消
//                    if (cancellationSignal.isCanceled()) {
//                        writeResultCallback.onWriteCancelled();
//                        mPdfDocument.close();
//                        mPdfDocument = null;
//                        return;
//                    }
//                    // Draw page content for printing
//                    // 绘制打印页面内容
//                    drawPage(page);
////                    // Rendering is complete, so page can be finalized.
//                    //渲染完成，因此可以完成页面。
//                    mPdfDocument.finishPage(page);
//                }
//            }
//
//            // Write PDF document to file
//            // 将PDF文档写入文件
//            try {
//                mPdfDocument.writeTo(new FileOutputStream(
//                        parcelFileDescriptor.getFileDescriptor()));
//            } catch (IOException e) {
//                writeResultCallback.onWriteFailed(e.toString());
//                return;
//            } finally {
//                mPdfDocument.close();
//                mPdfDocument = null;
//            }
//            PageRange[] writtenPages = computeWrittenPages();
//            // Signal the print framework the document is complete
//            //通知打印框架文档已完成
//            writeResultCallback.onWriteFinished(writtenPages);

        }
        //一旦打印进程结束后，该函数将会被调用。如果我们的应用有任何
        // 一次性销毁任务要执行，让这些任务在该方法内执行。这个回调方法不是必须实现的。
        @Override
        public void onFinish() {
            Toast.makeText(mContext, "已发送打印", Toast.LENGTH_SHORT).show();
            super.onFinish();
        }


        private void drawPage(PdfDocument.Page page) {
            Canvas canvas = page.getCanvas();
            // units are in points (1/72 of an inch)
            //单位是磅（1/72英寸）
            int titleBaseLine = 72;
            int leftMargin = 54;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(36);
            canvas.drawText("Test Title", leftMargin, titleBaseLine, paint);

            paint.setTextSize(11);
            canvas.drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint);

            paint.setColor(Color.BLUE);
            canvas.drawRect(100, 100, 172, 172, paint);
        }
    }
//    private int computePageCount(PrintAttributes printAttributes) {
//        // default item count for portrait mode
//        // 纵向模式的默认项目计数
//        int itemsPerPage = 4;
//        PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
//        if (!pageSize.isPortrait()) {
//            // Six items per page in landscape orientation
//            //横向方向每页六项
//            itemsPerPage = 6;
//        }
//
//        // Determine number of print items
//        //确定打印项目数
////        int printItemCount = getPrintItemCount();
//
//        return (int) Math.ceil(printItemCount / itemsPerPage);
//    }

    /**
     * 打开权限
     */
    private void openPermissions(){
        final RxPermissions rxPermissions = new RxPermissions(MainActivity.this); // where this is an Activity or Fragment instance
        rxPermissions.requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ).subscribe(new Consumer<Permission>() {
            @Override
            public void accept(Permission permission) throws Exception {
                if (permission.granted){
                    Log.d("执行","权限都通过了");
                }else if (permission.shouldShowRequestPermissionRationale){
                    Log.d("执行","至少有一个权限被拒绝了");
                    openPermissions();
                }else {
                    Log.d("执行","转到设置");
                }
            }
        });

    }
}
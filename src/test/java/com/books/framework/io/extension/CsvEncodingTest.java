package com.books.framework.io.extension;

import org.junit.jupiter.api.Test;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;

class CsvEncodingTest {

    @Test
    void utf8PlainShouldStayUtf8() {
        String input = "交易时间,交易对方,商品,金额\n2024-01-01,美团,外卖,12.50";
        byte[] utf8 = input.getBytes(StandardCharsets.UTF_8);
        byte[] result = transcodeToUtf8(utf8);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(input);
    }

    @Test
    void gbkEncodedShouldBecomeUtf8() {
        String text = "交易时间,交易对方\n2024-01-01,美团外卖";
        byte[] gbk = text.getBytes(Charset.forName("GBK"));
        byte[] result = transcodeToUtf8(gbk);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(text);
    }

    @Test
    void utf8BomShouldBeStripped() {
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String content = "交易时间,商品\n2024-01-01,美团";
        byte[] input = concat(bom, content.getBytes(StandardCharsets.UTF_8));
        byte[] result = transcodeToUtf8(input);
        assertThat(result.length).isLessThan(input.length);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @Test
    void gbkShortChineseShouldNotBeUtf8() {
        // GBK encoded "美团外卖" should be detected as non-UTF-8
        byte[] gbk = "美团外卖".getBytes(Charset.forName("GBK"));
        byte[] result = transcodeToUtf8(gbk);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("美团外卖");
    }

    @Test
    void gbkLargeTextShouldNotBeUtf8() {
        StringBuilder sb = new StringBuilder();
        sb.append("交易时间,交易对方,商品,金额,支付方式\n");
        for (int i = 0; i < 50; i++) {
            sb.append("2024-01-01,美团外卖,外卖订单,12.50,微信支付\n");
        }
        byte[] gbk = sb.toString().getBytes(Charset.forName("GBK"));
        byte[] result = transcodeToUtf8(gbk);
        String decoded = new String(result, StandardCharsets.UTF_8);
        assertThat(decoded).contains("美团外卖");
        assertThat(decoded).contains("微信支付");
    }

    @Test
    void utf8LargeTextShouldStayUtf8() {
        StringBuilder sb = new StringBuilder();
        sb.append("交易时间,交易对方,商品,金额,支付方式\n");
        for (int i = 0; i < 50; i++) {
            sb.append("2024-01-01,美团外卖,外卖订单,12.50,微信支付\n");
        }
        byte[] utf8 = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = transcodeToUtf8(utf8);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(sb.toString());
    }

    private static byte[] transcodeToUtf8(byte[] raw) {
        try {
            var m = CsvResourceLoader.class.getDeclaredMethod("toUtf8", byte[].class);
            m.setAccessible(true);
            return (byte[]) m.invoke(null, (Object) raw);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}

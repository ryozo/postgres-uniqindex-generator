package net.equj65.indexgenerator.generator;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import com.google.common.io.Resources;

/**
 * {@link UniqueIndexGenerator}のテストクラス。
 * @author W.Ryozo
 */
public class UniqueIndexGeneratorTest {
    
    @Test
    public void Test1_単一SQL単項目UNIQUE制約での動作確認() {
        URL inputSqlURL =  Resources.getResource("Test1_Input_単一SQL単項目UNIQUE.sql");
        URL expectSqlURL =  Resources.getResource("Test1_Expect_単一SQL単項目UNIQUE.sql");
        try {
        } catch (Throwable t) {
            fail();
        }
    }

}

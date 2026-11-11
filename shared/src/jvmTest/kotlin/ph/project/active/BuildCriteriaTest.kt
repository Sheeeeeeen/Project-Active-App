package ph.project.active

import kotlin.test.Test
import kotlin.test.assertFailsWith

class BuildCriteriaTest {

    @Test
    fun AC85_phase1_test_classes_remain_on_the_jvm_test_classpath() {
        Class.forName("ph.project.active.domain.RegisterAndSignInTest")
        Class.forName("ph.project.active.domain.SessionGenerationTest")
        Class.forName("ph.project.active.domain.RecordAttendanceTest")
        Class.forName("ph.com.sheen.projectactive.SharedCommonTest")
    }

    @Test
    fun AC86_sqldelight_and_coroutines_test_resolve() {
        Class.forName("app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver")
        Class.forName("app.cash.sqldelight.db.SqlDriver")
        Class.forName("kotlinx.coroutines.test.TestScope")
        Class.forName("kotlinx.coroutines.flow.StateFlow")
        Class.forName("kotlinx.coroutines.CoroutineScope")
    }

    @Test
    fun AC87_firebase_sdk_is_not_on_the_classpath() {
        assertFailsWith<ClassNotFoundException> { Class.forName("com.google.firebase.FirebaseApp") }
        assertFailsWith<ClassNotFoundException> { Class.forName("com.google.firebase.auth.FirebaseAuth") }
        assertFailsWith<ClassNotFoundException> { Class.forName("com.google.firebase.firestore.FirebaseFirestore") }
    }
}

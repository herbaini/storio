package com.pushtorefresh.storio.contentresolver.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.pushtorefresh.storio.contentresolver.BuildConfig;
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.operations.delete.DeleteResolver;
import com.pushtorefresh.storio.contentresolver.operations.get.GetResolver;
import com.pushtorefresh.storio.contentresolver.operations.put.PutResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;

import rx.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.schedulers.Schedulers.io;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DefaultStorIOContentResolverTest {

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void nullContentResolver() {
        DefaultStorIOContentResolver.builder()
                .contentResolver(null);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Test(expected = NullPointerException.class)
    public void addTypeMappingNullType() {
        DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(null, ContentResolverTypeMapping.builder()
                        .putResolver(mock(PutResolver.class))
                        .getResolver(mock(GetResolver.class))
                        .deleteResolver(mock(DeleteResolver.class))
                        .build());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void addTypeMappingNullMapping() {
        DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(Object.class, null);
    }

    @Test
    public void shouldReturnNullIfNoTypeMappingsRegistered() {
        class TestItem {

        }

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .build();

        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItem.class)).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnNullIfNotTypeMappingRegisteredForType() {
        class TestItem {

        }

        class Entity {

        }

        final ContentResolverTypeMapping<Entity> entityContentResolverTypeMapping = ContentResolverTypeMapping.<Entity>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(Entity.class, entityContentResolverTypeMapping)
                .build();

        assertThat(storIOContentResolver.lowLevel().typeMapping(Entity.class)).isSameAs(entityContentResolverTypeMapping);

        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItem.class)).isNull();
    }

    @Test
    public void directTypeMappingShouldWork() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .build();

        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItem.class)).isSameAs(typeMapping);
    }

    @Test
    public void indirectTypeMappingShouldWork() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .build();

        class TestItemSubclass extends TestItem {

        }

        // Direct type mapping should still work
        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItem.class)).isSameAs(typeMapping);

        // Indirect type mapping should give same type mapping as for parent class
        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItemSubclass.class)).isSameAs(typeMapping);
    }

    @Test
    public void indirectTypeMappingShouldCacheValue() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .build();

        class TestItemSubclass extends TestItem {

        }

        // Indirect type mapping should give same type mapping as for parent class
        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItemSubclass.class)).isSameAs(typeMapping);

        // Next call should be faster (we can not check this exactly)
        // But test coverage tool will check that we executed cache branch
        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItemSubclass.class)).isSameAs(typeMapping);
    }

    @Test
    public void typeMappingShouldWorkInCaseOfMoreConcreteTypeMapping() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        class TestItemSubclass extends TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItemSubclass> subclassTypeMapping = ContentResolverTypeMapping.<TestItemSubclass>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .addTypeMapping(TestItemSubclass.class, subclassTypeMapping)
                .build();

        // Parent class should have its own type mapping
        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItem.class)).isSameAs(typeMapping);

        // Child class should have its own type mapping
        assertThat(storIOContentResolver.lowLevel().typeMapping(TestItemSubclass.class)).isSameAs(subclassTypeMapping);
    }

    @Test
    public void typeMappingShouldFindIndirectTypeMappingInCaseOfComplexInheritance() {
        // Good test case — inheritance with AutoValue/AutoParcel

        class Entity {

        }

        class AutoValue_Entity extends Entity {

        }

        class ConcreteEntity extends Entity {

        }

        class AutoValue_ConcreteEntity extends ConcreteEntity {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<Entity> entitySQLiteTypeMapping = ContentResolverTypeMapping.<Entity>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        //noinspection unchecked
        final ContentResolverTypeMapping<ConcreteEntity> concreteEntitySQLiteTypeMapping = ContentResolverTypeMapping.<ConcreteEntity>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(Entity.class, entitySQLiteTypeMapping)
                .addTypeMapping(ConcreteEntity.class, concreteEntitySQLiteTypeMapping)
                .build();

        // Direct type mapping for Entity should work
        assertThat(storIOContentResolver.lowLevel().typeMapping(Entity.class)).isSameAs(entitySQLiteTypeMapping);

        // Direct type mapping for ConcreteEntity should work
        assertThat(storIOContentResolver.lowLevel().typeMapping(ConcreteEntity.class)).isSameAs(concreteEntitySQLiteTypeMapping);

        // Indirect type mapping for AutoValue_Entity should get type mapping for Entity
        assertThat(storIOContentResolver.lowLevel().typeMapping(AutoValue_Entity.class)).isSameAs(entitySQLiteTypeMapping);

        // Indirect type mapping for AutoValue_ConcreteEntity should get type mapping for ConcreteEntity, not for Entity!
        assertThat(storIOContentResolver.lowLevel().typeMapping(AutoValue_ConcreteEntity.class)).isSameAs(concreteEntitySQLiteTypeMapping);
    }

    interface InterfaceEntity {
    }

    @Test
    public void typeMappingShouldFindInterface() {
        //noinspection unchecked
        ContentResolverTypeMapping<InterfaceEntity> typeMapping = mock(ContentResolverTypeMapping.class);

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(InterfaceEntity.class, typeMapping)
                .build();

        assertThat(storIOContentResolver.lowLevel().typeMapping(InterfaceEntity.class)).isSameAs(typeMapping);
    }

    @Test
    public void typeMappingShouldFindIndirectTypeMappingForClassThatImplementsKnownInterface() {
        //noinspection unchecked
        ContentResolverTypeMapping<InterfaceEntity> typeMapping = mock(ContentResolverTypeMapping.class);

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(InterfaceEntity.class, typeMapping)
                .build();

        class ConcreteEntity implements InterfaceEntity {
        }

        assertThat(storIOContentResolver.lowLevel().typeMapping(ConcreteEntity.class)).isSameAs(typeMapping);

        // Just to make sure that we don't return this type mapping for all classes.
        assertThat(storIOContentResolver.lowLevel().typeMapping(Random.class)).isNull();
    }

    @Test
    public void typeMappingShouldFindIndirectTypeMappingForClassThatHasParentThatImplementsKnownInterface() {
        //noinspection unchecked
        ContentResolverTypeMapping<InterfaceEntity> typeMapping = mock(ContentResolverTypeMapping.class);

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(InterfaceEntity.class, typeMapping)
                .build();

        class ConcreteEntity implements InterfaceEntity {
        }

        class ParentConcreteEntity extends ConcreteEntity {
        }


        assertThat(storIOContentResolver.lowLevel().typeMapping(ParentConcreteEntity.class)).isSameAs(typeMapping);

        // Just to make sure that we don't return this type mapping for all classes.
        assertThat(storIOContentResolver.lowLevel().typeMapping(Random.class)).isNull();
    }

    @Test
    public void shouldThrowExceptionIfContentResolverReturnsNull() {
        ContentResolver contentResolver = mock(ContentResolver.class);

        StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(contentResolver)
                .build();

        Query query = Query.builder()
                .uri(mock(Uri.class))
                .build();

        when(contentResolver
                .query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString()))
                .thenReturn(null); // Notice, we return null instead of Cursor

        try {
            storIOContentResolver
                    .lowLevel()
                    .query(query);
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessage("Cursor returned by content provider is null");
        }
    }

    @Test
    public void shouldReturnSameContentResolver() {
        ContentResolver contentResolver = mock(ContentResolver.class);

        StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(contentResolver)
                .build();

        assertThat(storIOContentResolver.lowLevel().contentResolver()).isSameAs(contentResolver);
    }

    @Test
    public void defaultSchedulerReturnsIOSchedulerIfNotSpecified() {
        StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .build();

        assertThat(storIOContentResolver.defaultScheduler()).isEqualTo(io());
    }

    @Test
    public void defaultSchedulerReturnsSpecifiedScheduler() {
        Scheduler scheduler = mock(Scheduler.class);
        StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .defaultScheduler(scheduler)
                .build();

        assertThat(storIOContentResolver.defaultScheduler()).isEqualTo(scheduler);
    }

    @Test
    public void defaultSchedulerReturnsNullIfSpecifiedSchedulerNull() {
        StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .defaultScheduler(null)
                .build();

        assertThat(storIOContentResolver.defaultScheduler()).isNull();
    }
}

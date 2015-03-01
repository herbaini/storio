package com.pushtorefresh.android.bamboostorage.unit_test.design;

import com.pushtorefresh.android.bamboostorage.operation.delete.DeleteByQueryResult;
import com.pushtorefresh.android.bamboostorage.operation.delete.DeleteCollectionOfObjectsResult;
import com.pushtorefresh.android.bamboostorage.operation.delete.DeleteObjectResult;
import com.pushtorefresh.android.bamboostorage.query.DeleteQueryBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class DeleteOperationDesignTest extends OperationDesignTest {

    @Test public void deleteObjectBlocking() {
        User user = new User();

        DeleteObjectResult<User> deleteResult = bambooStorage()
                .delete()
                .object(user)
                .withMapFunc(User.MAP_TO_DELETE_QUERY)
                .prepare()
                .executeAsBlocking();
    }

    @Test public void deleteObjectObservable() {
        User user = new User();

        Observable<DeleteObjectResult<User>> deleteResultObservable = bambooStorage()
                .delete()
                .object(user)
                .withMapFunc(User.MAP_TO_DELETE_QUERY)
                .prepare()
                .createObservable();
    }

    @Test public void deleteCollectionOfObjectsBlocking() {
        List<User> users = new ArrayList<>();

        DeleteCollectionOfObjectsResult<User> deleteResult = bambooStorage()
                .delete()
                .objects(users)
                .withMapFunc(User.MAP_TO_DELETE_QUERY)
                .prepare()
                .executeAsBlocking();
    }

    @Test public void deleteCollectionOfObjectsObservable() {
        List<User> users = new ArrayList<>();

        Observable<DeleteCollectionOfObjectsResult<User>> deleteResultObservable = bambooStorage()
                .delete()
                .objects(users)
                .withMapFunc(User.MAP_TO_DELETE_QUERY)
                .prepare()
                .createObservable();
    }

    @Test public void deleteByQueryBlocking() {
        DeleteByQueryResult deleteResult = bambooStorage()
                .delete()
                .byQuery(new DeleteQueryBuilder()
                        .table("users")
                        .where("email = ?")
                        .whereArgs("artem.zinnatullin@gmail.com")
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    @Test public void deleteByQueryObservable() {
        Observable<DeleteByQueryResult> deleteResultObservable = bambooStorage()
                .delete()
                .byQuery(new DeleteQueryBuilder()
                        .table("users")
                        .where("email = ?")
                        .whereArgs("artem.zinnatullin@gmail.com")
                        .build())
                .prepare()
                .createObservable();
    }
}

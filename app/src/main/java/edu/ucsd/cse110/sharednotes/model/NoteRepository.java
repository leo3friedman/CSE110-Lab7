package edu.ucsd.cse110.sharednotes.model;

import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

public class NoteRepository {
    private final NoteAPI api;
    private final NoteDao dao;

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
        this.api = new NoteAPI();
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     *
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) throws ExecutionException, InterruptedException, TimeoutException {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if (theirNote == null) return; // do nothing
            if (ourNote == null || ourNote.version < theirNote.version) {
                upsertLocal(theirNote);
            }
        };

        // If we get a local update, pass it on.
        System.out.println("checking for local update");
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        System.out.println("checking for remote update");
        note.addSource(getRemote(title), updateFromRemote);
        System.out.println("finished local and remote update checks");

        return note;
    }

    public void upsertSynced(Note note) throws IOException {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();
    }

    public void upsertLocal(Note note) {
        note.version = note.version + 1;
        dao.upsert(note);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============

    public LiveData<Note> getRemote(String title) throws ExecutionException, InterruptedException, TimeoutException {
        // TODO: Implement getRemote!
        // TODO: Set up polling background thread (MutableLiveData?)
        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.

        // Start by fetching the note from the server _once_ and feeding it into MutableLiveData.
        // Then, set up a background thread that will poll the server every 3 seconds.

        // You may (but don't have to) want to cache the LiveData's for each title, so that
        // you don't create a new polling thread every time you call getRemote with the same title.
        // You don't need to worry about killing background threads.
        System.out.println("Get remote ran!");
//        System.out.println(api.getNote(title).toJSON());
//        System.out.println("Print didn't crash it");

        return  new MutableLiveData<>(api.getNoteAsync(title).get(1, SECONDS));
//        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void upsertRemote(Note note) throws IOException {
        // TODO: Implement upsertRemote!
//        String noteJSON = note.toJSON();
//        System.out.println("JSON: " + api.putNote(noteJSON));
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

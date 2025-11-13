package com.se1853_jv.labverse.domain.db;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.Converter;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.annotation.repo.HighlightRepository;
import com.se1853_jv.labverse.domain.infrastructure.annotation.repo.NoteRepository;
import com.se1853_jv.labverse.domain.infrastructure.sync.model.SyncQueue;
import com.se1853_jv.labverse.domain.infrastructure.sync.repo.SyncQueueRepository;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.citation.repo.CitationRepository;
import com.se1853_jv.labverse.domain.infrastructure.collection.repo.CollectionRepository;
import com.se1853_jv.labverse.domain.infrastructure.institution.model.Institution;
import com.se1853_jv.labverse.domain.infrastructure.institution.repo.InstitutionRepository;
import com.se1853_jv.labverse.domain.infrastructure.notification.model.Notification;
import com.se1853_jv.labverse.domain.infrastructure.notification.repo.NotificationRepository;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.paper.repo.PaperRepository;
import com.se1853_jv.labverse.domain.infrastructure.readinglist.model.ReadingList;
import com.se1853_jv.labverse.domain.infrastructure.readinglist.repo.ReadingListRepository;
import com.se1853_jv.labverse.domain.infrastructure.role.model.Roles;
import com.se1853_jv.labverse.domain.infrastructure.role.repo.RoleRepository;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;
import com.se1853_jv.labverse.domain.infrastructure.tag.repo.TagRepository;
import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;
import com.se1853_jv.labverse.domain.infrastructure.team.repo.TeamRepository;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;
import com.se1853_jv.labverse.domain.infrastructure.user.repo.UserRepository;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;
import com.se1853_jv.labverse.domain.infrastructure.workflow.repo.ReadingWorkflowRepository;

import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;

@Database(
        entities = {
                Users.class,
                Roles.class,
                Collections.class,
                ReadingWorkflow.class,
                Team.class,
                Tag.class,
                ReadingList.class,
                PaperResearch.class,
                Notification.class,
                Institution.class,
                Citation.class,
                Highlight.class,
                Note.class,
                SyncQueue.class
        },
        version = 5
)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserRepository userRepository();

    public abstract RoleRepository roleRepository();

    public abstract CollectionRepository collectionRepository();

    public abstract ReadingWorkflowRepository readingWorkflowRepository();

    public abstract TeamRepository teamRepository();

    public abstract TagRepository tagRepository();

    public abstract ReadingListRepository readingListRepository();

    public abstract PaperRepository paperRepository();

    public abstract NotificationRepository notificationRepository();

    public abstract InstitutionRepository institutionRepository();

    public abstract CitationRepository citationRepository();

    public abstract HighlightRepository highlightRepository();

    public abstract NoteRepository noteRepository();

    public abstract SyncQueueRepository syncQueueRepository();
}

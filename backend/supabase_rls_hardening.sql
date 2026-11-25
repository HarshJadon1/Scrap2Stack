-- ==============================================================================
-- Scrap2Stack Production RLS Hardening Migration
-- Replaces permissive `USING (true)` policies from development setup with strict
-- `auth.uid()` ownership and project-membership policies.
-- Prerequisites: Run `backend/supabase_schema.sql` first, then run this script in
-- Supabase Dashboard -> SQL Editor.
-- ==============================================================================

-- 1. Profiles: authenticated read, owner-only write
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select profiles" ON public.profiles;
DROP POLICY IF EXISTS "Public insert profiles" ON public.profiles;
DROP POLICY IF EXISTS "Public update profiles" ON public.profiles;
DROP POLICY IF EXISTS "Authenticated select profiles" ON public.profiles;
DROP POLICY IF EXISTS "Owner insert profile" ON public.profiles;
DROP POLICY IF EXISTS "Owner update profile" ON public.profiles;

CREATE POLICY "Authenticated select profiles" ON public.profiles
    FOR SELECT USING (auth.uid() IS NOT NULL);
CREATE POLICY "Owner insert profile" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid()::TEXT = id);
CREATE POLICY "Owner update profile" ON public.profiles
    FOR UPDATE USING (auth.uid()::TEXT = id) WITH CHECK (auth.uid()::TEXT = id);

-- 2. Projects: public/authenticated discovery read, owner-only mutate
ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select projects" ON public.projects;
DROP POLICY IF EXISTS "Public insert projects" ON public.projects;
DROP POLICY IF EXISTS "Public update projects" ON public.projects;
DROP POLICY IF EXISTS "Authenticated select projects" ON public.projects;
DROP POLICY IF EXISTS "Owner insert projects" ON public.projects;
DROP POLICY IF EXISTS "Owner update projects" ON public.projects;
DROP POLICY IF EXISTS "Owner delete projects" ON public.projects;

CREATE POLICY "Authenticated select projects" ON public.projects
    FOR SELECT USING (auth.uid() IS NOT NULL);
CREATE POLICY "Owner insert projects" ON public.projects
    FOR INSERT WITH CHECK (auth.uid()::TEXT = owner_id);
CREATE POLICY "Owner update projects" ON public.projects
    FOR UPDATE USING (auth.uid()::TEXT = owner_id) WITH CHECK (auth.uid()::TEXT = owner_id);
CREATE POLICY "Owner delete projects" ON public.projects
    FOR DELETE USING (auth.uid()::TEXT = owner_id);

-- 3. Project Members: authenticated read, owner-managed membership (or SECURITY DEFINER RPC)
ALTER TABLE public.project_members ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select project_members" ON public.project_members;
DROP POLICY IF EXISTS "Public insert project_members" ON public.project_members;
DROP POLICY IF EXISTS "Authenticated select project_members" ON public.project_members;
DROP POLICY IF EXISTS "Project owner insert project_members" ON public.project_members;
DROP POLICY IF EXISTS "Project owner delete project_members" ON public.project_members;

CREATE POLICY "Authenticated select project_members" ON public.project_members
    FOR SELECT USING (auth.uid() IS NOT NULL);
CREATE POLICY "Project owner insert project_members" ON public.project_members
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Project owner delete project_members" ON public.project_members
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
    );

-- 4. Workspace Tasks: project owner or active team member
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select tasks" ON public.tasks;
DROP POLICY IF EXISTS "Public insert tasks" ON public.tasks;
DROP POLICY IF EXISTS "Public update tasks" ON public.tasks;
DROP POLICY IF EXISTS "Workspace select tasks" ON public.tasks;
DROP POLICY IF EXISTS "Workspace insert tasks" ON public.tasks;
DROP POLICY IF EXISTS "Workspace update tasks" ON public.tasks;
DROP POLICY IF EXISTS "Workspace delete tasks" ON public.tasks;

CREATE POLICY "Workspace select tasks" ON public.tasks
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = tasks.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Workspace insert tasks" ON public.tasks
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = tasks.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Workspace update tasks" ON public.tasks
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = tasks.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Workspace delete tasks" ON public.tasks
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
    );

-- 5. Team Chat Messages: project owner or team member, sender matches auth.uid()
ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select chat_messages" ON public.chat_messages;
DROP POLICY IF EXISTS "Public insert chat_messages" ON public.chat_messages;
DROP POLICY IF EXISTS "Workspace select chat_messages" ON public.chat_messages;
DROP POLICY IF EXISTS "Workspace insert chat_messages" ON public.chat_messages;

CREATE POLICY "Workspace select chat_messages" ON public.chat_messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = chat_messages.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Workspace insert chat_messages" ON public.chat_messages
    FOR INSERT WITH CHECK (
        auth.uid()::TEXT = sender_id
        AND (
            EXISTS (
                SELECT 1 FROM public.projects p
                WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
            )
            OR EXISTS (
                SELECT 1 FROM public.project_members pm
                WHERE pm.project_id = chat_messages.project_id AND pm.user_id = auth.uid()::TEXT
            )
        )
    );

-- 6. Collaboration Requests: sender or receiver only
ALTER TABLE public.collaboration_requests ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Public insert collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Public update collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Participant select collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Sender insert collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Participant update collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Sender delete collaboration_requests" ON public.collaboration_requests;

CREATE POLICY "Participant select collaboration_requests" ON public.collaboration_requests
    FOR SELECT USING (auth.uid()::TEXT = sender_id OR auth.uid()::TEXT = receiver_id);
CREATE POLICY "Sender insert collaboration_requests" ON public.collaboration_requests
    FOR INSERT WITH CHECK (auth.uid()::TEXT = sender_id);
CREATE POLICY "Participant update collaboration_requests" ON public.collaboration_requests
    FOR UPDATE USING (auth.uid()::TEXT = sender_id OR auth.uid()::TEXT = receiver_id);
CREATE POLICY "Sender delete collaboration_requests" ON public.collaboration_requests
    FOR DELETE USING (auth.uid()::TEXT = sender_id);

-- 7. Roadmaps & Roadmap Items: project owner or team member
ALTER TABLE public.roadmaps ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select roadmaps" ON public.roadmaps;
DROP POLICY IF EXISTS "Public insert roadmaps" ON public.roadmaps;
DROP POLICY IF EXISTS "Workspace select roadmaps" ON public.roadmaps;
DROP POLICY IF EXISTS "Workspace insert roadmaps" ON public.roadmaps;

CREATE POLICY "Workspace select roadmaps" ON public.roadmaps
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = roadmaps.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Workspace insert roadmaps" ON public.roadmaps
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = roadmaps.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );

ALTER TABLE public.roadmap_items ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select roadmap_items" ON public.roadmap_items;
DROP POLICY IF EXISTS "Public insert roadmap_items" ON public.roadmap_items;
DROP POLICY IF EXISTS "Workspace select roadmap_items" ON public.roadmap_items;
DROP POLICY IF EXISTS "Workspace insert roadmap_items" ON public.roadmap_items;

CREATE POLICY "Workspace select roadmap_items" ON public.roadmap_items
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = roadmap_items.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );
CREATE POLICY "Workspace insert roadmap_items" ON public.roadmap_items
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.projects p
            WHERE p.id::TEXT = project_id AND p.owner_id = auth.uid()::TEXT
        )
        OR EXISTS (
            SELECT 1 FROM public.project_members pm
            WHERE pm.project_id = roadmap_items.project_id AND pm.user_id = auth.uid()::TEXT
        )
    );

-- 8. Notifications: owner-only SELECT and UPDATE (inserted by SECURITY DEFINER RPCs)
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select notifications" ON public.notifications;
DROP POLICY IF EXISTS "Public insert notifications" ON public.notifications;
DROP POLICY IF EXISTS "Public update notifications" ON public.notifications;
DROP POLICY IF EXISTS "Owner select notifications" ON public.notifications;
DROP POLICY IF EXISTS "Owner update notifications" ON public.notifications;

CREATE POLICY "Owner select notifications" ON public.notifications
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Owner update notifications" ON public.notifications
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 9. User Charms Ledger: authenticated read, writes restricted to SECURITY DEFINER RPCs
ALTER TABLE public.user_charms ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select user_charms" ON public.user_charms;
DROP POLICY IF EXISTS "Public insert user_charms" ON public.user_charms;
DROP POLICY IF EXISTS "Authenticated select user_charms" ON public.user_charms;

CREATE POLICY "Authenticated select user_charms" ON public.user_charms
    FOR SELECT USING (auth.uid() IS NOT NULL);

-- Grant RPC execution strictly to authenticated users (and service_role)
REVOKE ALL ON FUNCTION public.respond_to_collaboration(UUID, TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.respond_to_collaboration(UUID, TEXT) TO authenticated, service_role;

REVOKE ALL ON FUNCTION public.award_task_completion_charms(UUID, UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.award_task_completion_charms(UUID, UUID) TO authenticated, service_role;

REVOKE ALL ON FUNCTION public.award_project_revived_charms(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.award_project_revived_charms(UUID) TO authenticated, service_role;

REVOKE ALL ON FUNCTION public.award_project_shipped_charms(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.award_project_shipped_charms(UUID) TO authenticated, service_role;

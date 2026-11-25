-- ==============================================================================
-- Scrap2Stack Supabase Database Setup & Permission Migration
-- Run this complete script in Supabase Dashboard -> SQL Editor -> New Query -> Run
-- ==============================================================================

-- 1. Grant Schema Permissions to anon, authenticated, and service_role
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL ROUTINES IN SCHEMA public TO anon, authenticated, service_role;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO anon, authenticated, service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO anon, authenticated, service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON ROUTINES TO anon, authenticated, service_role;

-- 2. Ensure `profiles` table has proper columns
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL DEFAULT '',
    username TEXT NOT NULL DEFAULT '',
    email TEXT,
    profile_image TEXT,
    bio TEXT,
    experience_level TEXT DEFAULT 'INTERMEDIATE',
    skills TEXT[] DEFAULT '{}',
    interests TEXT[] DEFAULT '{}',
    github_url TEXT,
    linkedin_url TEXT,
    portfolio_url TEXT,
    charms INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 3. Create or replace `developer_profiles` view if missing
CREATE OR REPLACE VIEW public.developer_profiles AS
SELECT 
    id,
    name,
    username,
    email,
    profile_image,
    bio,
    experience_level,
    skills,
    interests,
    github_url,
    linkedin_url,
    portfolio_url,
    charms,
    created_at,
    updated_at
FROM public.profiles;

-- 4. Ensure `projects` table
CREATE TABLE IF NOT EXISTS public.projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    problem TEXT,
    category TEXT,
    technologies TEXT[] DEFAULT '{}',
    required_skills TEXT[] DEFAULT '{}',
    status TEXT NOT NULL DEFAULT 'IDEA',
    github_url TEXT,
    github_repo_owner TEXT,
    github_repo_name TEXT,
    github_connected BOOLEAN DEFAULT false,
    github_connected_at TIMESTAMPTZ,
    github_default_branch TEXT,
    team_size INT DEFAULT 1,
    difficulty TEXT DEFAULT 'INTERMEDIATE',
    revival_score INT DEFAULT 50,
    quality_score INT DEFAULT 50,
    analysis_id TEXT,
    progress INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 5. Ensure `tasks` table
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id TEXT NOT NULL,
    title TEXT NOT NULL,
    assignee_id TEXT,
    priority TEXT DEFAULT 'MEDIUM',
    status TEXT DEFAULT 'TODO',
    skill TEXT,
    due_date TIMESTAMPTZ,
    created_by TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 6. Ensure `chat_messages` table (with Realtime)
CREATE TABLE IF NOT EXISTS public.chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id TEXT NOT NULL,
    sender_id TEXT NOT NULL,
    sender_name TEXT NOT NULL DEFAULT 'Team Member',
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 7. Ensure `user_charms` contribution table
CREATE TABLE IF NOT EXISTS public.user_charms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    project_id TEXT,
    contribution_type TEXT NOT NULL DEFAULT 'TASK_COMPLETION',
    charms INT NOT NULL DEFAULT 10,
    description TEXT NOT NULL DEFAULT 'Contribution awarded',
    reference_id TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 8. Ensure `collaboration_requests` table
CREATE TABLE IF NOT EXISTS public.collaboration_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id TEXT NOT NULL,
    sender_id TEXT NOT NULL,
    receiver_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 9. Ensure `project_members` table
CREATE TABLE IF NOT EXISTS public.project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ DEFAULT now()
);

-- 10. Ensure `roadmaps` & `roadmap_items` tables
CREATE TABLE IF NOT EXISTS public.roadmaps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id TEXT NOT NULL,
    workspace_id TEXT,
    title TEXT NOT NULL DEFAULT 'Project Roadmap',
    description TEXT,
    status TEXT DEFAULT 'ACTIVE',
    generated_by_ai BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.roadmap_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id TEXT,
    project_id TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    "order" INT DEFAULT 0,
    status TEXT DEFAULT 'PLANNED',
    required_skills TEXT[] DEFAULT '{}',
    required_roles TEXT[] DEFAULT '{}',
    estimated_effort TEXT,
    dependencies TEXT[] DEFAULT '{}',
    milestone BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 11. Row Level Security (RLS) Permissive Policies for Web & App
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select profiles" ON public.profiles;
DROP POLICY IF EXISTS "Public insert profiles" ON public.profiles;
DROP POLICY IF EXISTS "Public update profiles" ON public.profiles;
CREATE POLICY "Public select profiles" ON public.profiles FOR SELECT USING (true);
CREATE POLICY "Public insert profiles" ON public.profiles FOR INSERT WITH CHECK (true);
CREATE POLICY "Public update profiles" ON public.profiles FOR UPDATE USING (true);

ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select projects" ON public.projects;
DROP POLICY IF EXISTS "Public insert projects" ON public.projects;
DROP POLICY IF EXISTS "Public update projects" ON public.projects;
CREATE POLICY "Public select projects" ON public.projects FOR SELECT USING (true);
CREATE POLICY "Public insert projects" ON public.projects FOR INSERT WITH CHECK (true);
CREATE POLICY "Public update projects" ON public.projects FOR UPDATE USING (true);

ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select tasks" ON public.tasks;
DROP POLICY IF EXISTS "Public insert tasks" ON public.tasks;
DROP POLICY IF EXISTS "Public update tasks" ON public.tasks;
CREATE POLICY "Public select tasks" ON public.tasks FOR SELECT USING (true);
CREATE POLICY "Public insert tasks" ON public.tasks FOR INSERT WITH CHECK (true);
CREATE POLICY "Public update tasks" ON public.tasks FOR UPDATE USING (true);

ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select chat_messages" ON public.chat_messages;
DROP POLICY IF EXISTS "Public insert chat_messages" ON public.chat_messages;
CREATE POLICY "Public select chat_messages" ON public.chat_messages FOR SELECT USING (true);
CREATE POLICY "Public insert chat_messages" ON public.chat_messages FOR INSERT WITH CHECK (true);

ALTER TABLE public.user_charms ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select user_charms" ON public.user_charms;
DROP POLICY IF EXISTS "Public insert user_charms" ON public.user_charms;
CREATE POLICY "Public select user_charms" ON public.user_charms FOR SELECT USING (true);
CREATE POLICY "Public insert user_charms" ON public.user_charms FOR INSERT WITH CHECK (true);

ALTER TABLE public.collaboration_requests ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Public insert collaboration_requests" ON public.collaboration_requests;
DROP POLICY IF EXISTS "Public update collaboration_requests" ON public.collaboration_requests;
CREATE POLICY "Public select collaboration_requests" ON public.collaboration_requests FOR SELECT USING (true);
CREATE POLICY "Public insert collaboration_requests" ON public.collaboration_requests FOR INSERT WITH CHECK (true);
CREATE POLICY "Public update collaboration_requests" ON public.collaboration_requests FOR UPDATE USING (true);

ALTER TABLE public.project_members ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select project_members" ON public.project_members;
DROP POLICY IF EXISTS "Public insert project_members" ON public.project_members;
CREATE POLICY "Public select project_members" ON public.project_members FOR SELECT USING (true);
CREATE POLICY "Public insert project_members" ON public.project_members FOR INSERT WITH CHECK (true);

ALTER TABLE public.roadmaps ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select roadmaps" ON public.roadmaps;
DROP POLICY IF EXISTS "Public insert roadmaps" ON public.roadmaps;
CREATE POLICY "Public select roadmaps" ON public.roadmaps FOR SELECT USING (true);
CREATE POLICY "Public insert roadmaps" ON public.roadmaps FOR INSERT WITH CHECK (true);

ALTER TABLE public.roadmap_items ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select roadmap_items" ON public.roadmap_items;
DROP POLICY IF EXISTS "Public insert roadmap_items" ON public.roadmap_items;
CREATE POLICY "Public select roadmap_items" ON public.roadmap_items FOR SELECT USING (true);
CREATE POLICY "Public insert roadmap_items" ON public.roadmap_items FOR INSERT WITH CHECK (true);

-- 11b. Ensure `collaboration_requests` has `proposed_role` & create `notifications`
ALTER TABLE public.collaboration_requests
    ADD COLUMN IF NOT EXISTS proposed_role TEXT DEFAULT 'CONTRIBUTOR';

CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'SYSTEM',
    read BOOLEAN NOT NULL DEFAULT false,
    reference_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public select notifications" ON public.notifications;
DROP POLICY IF EXISTS "Public insert notifications" ON public.notifications;
DROP POLICY IF EXISTS "Public update notifications" ON public.notifications;
CREATE POLICY "Public select notifications" ON public.notifications FOR SELECT USING (true);
CREATE POLICY "Public insert notifications" ON public.notifications FOR INSERT WITH CHECK (true);
CREATE POLICY "Public update notifications" ON public.notifications FOR UPDATE USING (true);

-- 11c. Backend RPC Functions (Collaboration & Charm Rewards)
CREATE OR REPLACE FUNCTION public.respond_to_collaboration(
    req_id UUID,
    response_status TEXT
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_req RECORD;
    v_project_owner TEXT;
    v_project_name TEXT;
    v_new_member TEXT;
    v_normalized_status TEXT;
BEGIN
    SELECT * INTO v_req FROM public.collaboration_requests WHERE id = req_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Collaboration request % not found', req_id;
    END IF;

    v_normalized_status := UPPER(COALESCE(response_status, 'PENDING'));
    UPDATE public.collaboration_requests
    SET status = v_normalized_status, updated_at = now()
    WHERE id = req_id;

    SELECT owner_id, name INTO v_project_owner, v_project_name
    FROM public.projects
    WHERE id::TEXT = v_req.project_id;

    IF v_normalized_status = 'ACCEPTED' THEN
        v_new_member := CASE
            WHEN v_req.sender_id = v_project_owner THEN v_req.receiver_id
            ELSE v_req.sender_id
        END;

        IF NOT EXISTS (
            SELECT 1 FROM public.project_members
            WHERE project_id = v_req.project_id AND user_id = v_new_member
        ) THEN
            INSERT INTO public.project_members (project_id, user_id, role)
            VALUES (
                v_req.project_id,
                v_new_member,
                LOWER(COALESCE(NULLIF(v_req.proposed_role, ''), 'contributor'))
            );
        END IF;

        IF v_req.sender_id ~ '^[0-9a-fA-F-]{36}$' THEN
            INSERT INTO public.notifications (user_id, title, content, type, reference_id)
            VALUES (
                v_req.sender_id::UUID,
                'Collaboration Accepted',
                'Your request for ' || COALESCE(v_project_name, 'project') || ' was accepted.',
                'COLLABORATION',
                v_req.project_id
            );
        END IF;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION public.award_task_completion_charms(
    p_task_id UUID,
    p_project_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_uid UUID := auth.uid();
    v_uid_text TEXT := auth.uid()::TEXT;
    v_project_name TEXT;
BEGIN
    IF v_uid IS NULL THEN
        RETURN;
    END IF;

    SELECT name INTO v_project_name FROM public.projects WHERE id = p_project_id;

    INSERT INTO public.user_charms (user_id, project_id, charms, contribution_type, description, reference_id)
    VALUES (
        v_uid_text,
        p_project_id::TEXT,
        10,
        'TASK_COMPLETION',
        'Completed task in ' || COALESCE(v_project_name, 'workspace'),
        p_task_id::TEXT
    );

    UPDATE public.profiles
    SET charms = COALESCE(charms, 0) + 10
    WHERE id = v_uid_text;

    INSERT INTO public.notifications (user_id, title, content, type, reference_id)
    VALUES (
        v_uid,
        '+10 Charms Earned',
        'Task completed in ' || COALESCE(v_project_name, 'workspace') || '.',
        'CHARMS',
        p_project_id::TEXT
    );
END;
$$;

CREATE OR REPLACE FUNCTION public.award_project_revived_charms(
    p_project_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_uid UUID := auth.uid();
    v_uid_text TEXT := auth.uid()::TEXT;
    v_project_name TEXT;
BEGIN
    IF v_uid IS NULL THEN
        RETURN;
    END IF;

    SELECT name INTO v_project_name FROM public.projects WHERE id = p_project_id;

    INSERT INTO public.user_charms (user_id, project_id, charms, contribution_type, description, reference_id)
    VALUES (
        v_uid_text,
        p_project_id::TEXT,
        50,
        'PROJECT_REVIVED',
        'Initiated revival for ' || COALESCE(v_project_name, 'project'),
        p_project_id::TEXT
    );

    UPDATE public.profiles
    SET charms = COALESCE(charms, 0) + 50
    WHERE id = v_uid_text;
END;
$$;

CREATE OR REPLACE FUNCTION public.award_project_shipped_charms(
    p_project_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_member RECORD;
    v_project_name TEXT;
BEGIN
    SELECT name INTO v_project_name FROM public.projects WHERE id = p_project_id;

    FOR v_member IN
        SELECT DISTINCT user_id FROM public.project_members WHERE project_id = p_project_id::TEXT
    LOOP
        INSERT INTO public.user_charms (user_id, project_id, charms, contribution_type, description, reference_id)
        VALUES (
            v_member.user_id,
            p_project_id::TEXT,
            100,
            'PROJECT_SHIPPED',
            'Shipped ' || COALESCE(v_project_name, 'project') || ' to Showcase!',
            p_project_id::TEXT
        );

        UPDATE public.profiles
        SET charms = COALESCE(charms, 0) + 100
        WHERE id = v_member.user_id;

        IF v_member.user_id ~ '^[0-9a-fA-F-]{36}$' THEN
            INSERT INTO public.notifications (user_id, title, content, type, reference_id)
            VALUES (
                v_member.user_id::UUID,
                '+100 Charms: Project Shipped!',
                COALESCE(v_project_name, 'Project') || ' was published to Showcase.',
                'MILESTONE',
                p_project_id::TEXT
            );
        END IF;
    END LOOP;
END;
$$;

-- 12. Add Realtime Publications for Live Sync (Chat, Tasks, Collab, Notifications)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'chat_messages') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.chat_messages;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'tasks') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.tasks;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'collaboration_requests') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.collaboration_requests;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'notifications') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
    END IF;
EXCEPTION
    WHEN others THEN NULL;
END $$;

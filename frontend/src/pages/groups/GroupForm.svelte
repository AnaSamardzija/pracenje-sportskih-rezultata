<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {createGroup, getGroup, updateGroup} from '../../lib/api/groups.api';
    import PageHeader from '../../lib/components/PageHeader.svelte';

    // Ista forma za novu grupu (/groups/new) i izmenu (/groups/:id/edit)
    let {params}: {params?: {id?: string}} = $props();

    const editId = $derived(params?.id ?? null);
    const isEdit = $derived(editId !== null);

    let name = $state('');
    let description = $state('');

    let loadingData = $state(false);
    let loadFailed = $state(false);
    let saving = $state(false);
    let error = $state<string | null>(null);

    // Izmenu dozvoljava samo GROUP_ADMIN; backend bi ionako vratio 403, ali ovako korisnik ne popunjava formu uzalud
    onMount(async () => {
        if (!editId) return;
        loadingData = true;
        try {
            const group = await getGroup(editId);
            if (group.myRole !== 'GROUP_ADMIN') {
                error = 'Only a group admin can edit this group';
                loadFailed = true;
                return;
            }
            name = group.name;
            description = group.description ?? '';
        } catch (e) {
            error = e instanceof Error ? e.message : 'Group not found';
            loadFailed = true;
        } finally {
            loadingData = false;
        }
    });

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        error = null;
        saving = true;

        const data = {name: name.trim(), description: description.trim() || null};

        try {
            const saved = editId ? await updateGroup(editId, data) : await createGroup(data);
            push(`/groups/${saved.id}`);
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to save group';
        } finally {
            saving = false;
        }
    }
</script>

<PageHeader title={isEdit ? 'Edit group' : 'New group'}
            icon={isEdit ? 'bi-pencil-square' : 'bi-plus-circle'}
            subtitle={isEdit ? 'Change the name or description of your group.' : 'You will become the admin of the new group and can add members right away.'}/>

<div class="container py-4 page-fade">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-xl-7">
            {#if error}
                <div class="alert alert-danger d-flex align-items-center">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            {/if}

            {#if loadingData}
                <div class="text-center py-5">
                    <div class="spinner-border text-primary"></div>
                </div>
            {:else if loadFailed}
                <a class="btn btn-outline-secondary" href={editId ? `#/groups/${editId}` : '#/groups'}>
                    <i class="bi bi-arrow-left me-1"></i>Back to group
                </a>
            {:else}
                <form onsubmit={handleSubmit}>
                    <div class="card mb-4">
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label" for="name">Name</label>
                                <input id="name" class="form-control" bind:value={name} required/>
                            </div>
                            <div>
                                <label class="form-label" for="description">Description <span class="text-muted small">(optional)</span></label>
                                <textarea id="description" class="form-control" rows="3" bind:value={description}></textarea>
                            </div>
                        </div>
                    </div>

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary" disabled={saving}>
                            {#if saving}
                                <span class="spinner-border spinner-border-sm me-2"></span>Saving...
                            {:else}
                                <i class="bi bi-check-lg me-2"></i>{isEdit ? 'Save changes' : 'Create group'}
                            {/if}
                        </button>
                        <a class="btn btn-outline-secondary" href={editId ? `#/groups/${editId}` : '#/groups'}>Cancel</a>
                    </div>
                </form>
            {/if}
        </div>
    </div>
</div>

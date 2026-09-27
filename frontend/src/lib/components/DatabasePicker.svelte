<script lang="ts">
    import type {StorageType} from '../types/auth.types';

    // Izbor baze za prijavu i registraciju; roditelj izabranu vrednost dobija preko bind:value
    let {value = $bindable('MARIADB')}: {value?: StorageType} = $props();
</script>

<!-- Vrednosti su tačno enum stringovi koje backend očekuje u storageType -->
<fieldset class="mb-4">
    <legend class="form-label fs-6">Database</legend>
    <div class="btn-group w-100">
        <input id="db-maria" class="btn-check" type="radio" value="MARIADB" bind:group={value}/>
        <label class="btn btn-outline-primary" for="db-maria">
            <i class="bi bi-database me-1"></i>MariaDB
        </label>
        <input id="db-mongo" class="btn-check" type="radio" value="MONGODB" bind:group={value}/>
        <label class="btn btn-outline-primary" for="db-mongo">
            <i class="bi bi-database me-1"></i>MongoDB
        </label>
    </div>
</fieldset>

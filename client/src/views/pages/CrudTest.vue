<script setup>
import GenericCrud from '@/components/GenericCrud.vue';
import testService from '@/service/TestService';
import { testFields, createEmptyTest } from '@/service/TestService';
</script>

<template>
    <GenericCrud
        dialogHeader="Test Details"
        dataKey="id"
        :fields="testFields"
        :service="testService"
        :createEmptyRecord="createEmptyTest"
        :messages="{
            created: 'Test Created',
            updated: 'Test Updated',
            deleted: 'Test Deleted',
            deletedMany: 'Tests Deleted'
        }"
    />
</template>

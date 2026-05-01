<script setup>
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import Calendar from 'primevue/calendar';

const props = defineProps({
  fields: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: () => ({})
  },
  submitted: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

const updateField = (fieldName, value) => {
  emit('update:modelValue', {
    ...props.modelValue,
    [fieldName]: value
  });
};

const getFieldOptions = (field) => {
  return field.options || [];
};

const getOptionLabel = (field) => {
  return field.optionLabel || 'label';
};

const getOptionValue = (field) => {
  return field.optionValue || 'value';
};

const isEmptyValue = (value) => {
  if (typeof value === 'string') {
    return value.trim() === '';
  }

  return value === null || value === undefined;
};

const hasFieldError = (field) => {
  return props.submitted && field.required && isEmptyValue(props.modelValue[field.name]);
};
</script>

<template>
  <template v-for="field in fields" :key="field.name">
    <input
        v-if="field.hidden"
        :id="field.name"
        :value="modelValue[field.name]"
        type="hidden"
        @input="updateField(field.name, $event.target.value)"
    />

    <div v-else-if="!field.editable" class="field">
      <label :for="field.name">{{ field.label }}</label>
      <InputText :id="field.name" :modelValue="modelValue[field.name]" disabled/>
    </div>

    <div v-else-if="field.type === 'text'" class="field">
      <label :for="field.name">{{ field.label }}</label>
      <InputText
          :id="field.name"
          :modelValue="modelValue[field.name]"
          :class="{ 'p-invalid': hasFieldError(field) }"
          @update:modelValue="updateField(field.name, $event)"
      />
      <small v-if="hasFieldError(field)" class="p-invalid">
        {{ field.label }} is required.
      </small>
    </div>

    <div v-else-if="field.type === 'number'" class="field">
      <label :for="field.name">{{ field.label }}</label>
      <InputNumber
          :id="field.name"
          :modelValue="modelValue[field.name]"
          integeronly
          :class="{ 'p-invalid': hasFieldError(field) }"
          @update:modelValue="updateField(field.name, $event)"
      />
      <small v-if="hasFieldError(field)" class="p-invalid">
        {{ field.label }} is required.
      </small>
    </div>

    <div v-else-if="field.type === 'enum'" class="field">
      <label :for="field.name">{{ field.label }}</label>
      <Dropdown
          :id="field.name"
          :modelValue="modelValue[field.name]"
          :options="getFieldOptions(field)"
          :optionLabel="getOptionLabel(field)"
          :optionValue="getOptionValue(field)"
          :placeholder="field.placeholder || `Select ${field.label}`"
          :class="{ 'p-invalid': hasFieldError(field) }"
          @update:modelValue="updateField(field.name, $event)"
      />
      <small v-if="hasFieldError(field)" class="p-invalid">
        {{ field.label }} is required.
      </small>
    </div>

    <div v-else-if="field.type === 'textarea'" class="field">
      <label :for="field.name">{{ field.label }}</label>
      <Textarea
          :id="field.name"
          :modelValue="modelValue[field.name]"
          rows="3"
          cols="20"
          :class="{ 'p-invalid': hasFieldError(field) }"
          @update:modelValue="updateField(field.name, $event)"
      />
      <small v-if="hasFieldError(field)" class="p-invalid">
        {{ field.label }} is required.
      </small>
    </div>

    <div v-else-if="field.type === 'date'" class="field">
      <label :for="field.name">{{ field.label }}</label>
      <Calendar
          :id="field.name"
          :modelValue="modelValue[field.name]"
          :class="{ 'p-invalid': hasFieldError(field) }"
          @update:modelValue="updateField(field.name, $event)"
      />
      <small v-if="hasFieldError(field)" class="p-invalid">
        {{ field.label }} is required.
      </small>
    </div>
  </template>
</template>
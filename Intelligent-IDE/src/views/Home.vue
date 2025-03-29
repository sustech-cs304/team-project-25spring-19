<template>
  <div class="home">
    <h1>Welcome to Intelligent Course-Aware IDE</h1>
    <p>Navigate to the Course Dashboard to get started!</p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'Home',
})
</script>

<style scoped>
.home {
  text-align: center;
  padding: 50px;
}
h1 {
  color: #2c3e50;
}
</style>

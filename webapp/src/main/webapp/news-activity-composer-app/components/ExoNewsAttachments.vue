<template>
  <div>
    <div class="VuetifyApp">
      <v-app>
        <v-btn
          class="attachmentsButton"
          fixed
          bottom
          right
          fab
          x-large
          @click="showAttachments = !showAttachments"
        >
          <i class="uiIconAttachment"></i>
          <v-progress-circular
            :class="uploading ? 'uploading' : ''"
            indeterminate>
            {{ value.length }}
          </v-progress-circular>
        </v-btn>
      </v-app>
    </div>
    <div :class="showAttachments ? 'open' : ''" class="newsAttachments drawer" @keydown.esc="closeAttachments()">
      <div :class="showDocumentSelector? 'documentSelector' : ''" class="attachmentsHeader header">
        <a v-if="showDocumentSelector" class="backButton" @click="toggleServerFileSelector()">
          <i class="uiIconBack"> </i>
        </a>
        <span class="attachmentsTitle">{{ drawerTitle }}</span>
        <a class="attachmentsCloseIcon" @click="closeAttachments()">×</a>
      </div>
      <div :class="showDocumentSelector? 'serverFiles' : 'newsAttachments'" class="content">
        <div v-if="!showDocumentSelector" class="newsAttachmentsContent">
          <div class="multiploadFilesSelector">
            <div id="DropFileBox" ref="dropFileBox" class="dropFileBox">
              <span class="dropMsg">{{ $t('news.composer.attachments.drop') }}</span>
              <span>
                <a :title="$t('news.composer.attachments.upload')" class="uploadButton" href="#" rel="tooltip" data-placement="bottom" @click="uploadFile">
                  <span class="text">{{ $t('news.composer.attachments.upload') }}</span>
                  <span class="mobileText">{{ $t('news.composer.attachments.upload') }}</span>
                </a>
              </span>
              <span class="or">{{ $t('news.composer.attachments.or') }}</span>
              <span>
                <a title="Select on server" class="uploadButton" href="#" rel="tooltip" data-placement="bottom" @click="toggleServerFileSelector()">
                  <span class="text">{{ $t('news.composer.attachments.existingUploads') }}</span>
                </a>
              </span>
            </div>
            <div class="fileHidden" style="display:none">
              <input ref="uploadInput" class="file" name="file" type="file" multiple="multiple" style="display:none" @change="handleFileUpload($refs.uploadInput.files)">
            </div>
            <transition name="fade">
              <div v-show="fileSizeLimitError" class="sizeExceeded alert alert-error">
                <i class="uiIconError"></i>
                {{ $t('news.composer.attachments.maxFileSize.error').replace('{0}', maxFileSize) }}
              </div>
            </transition>
            <transition name="fade">
              <div v-show="filesCountLimitError" class="countExceeded alert alert-error">
                <i class="uiIconError"></i>
                {{ $t('news.composer.attachments.maxFileCount.error').replace('{0}', maxFilesCount) }}
              </div>
            </transition>
          </div>

          <div class="limitMessage">
            <div class="sizeLimit">{{ $t('news.composer.attachments.maxFileSize').replace('{0}', maxFileSize) }}</div>
            <div class="countLimit">{{ $t('news.composer.attachments.maxFileCount').replace('{0}', maxFilesCount) }}</div>
          </div>

          <div class="uploadedFiles">
            <div class="uploadedFilesTitle">{{ $t('news.composer.attachments.title') }} ({{ value.length }})</div>
            <div class="uploadedFilesItems">
              <div v-for="attachedFile in value" :key="attachedFile.name" class="uploadedFilesItem">
                <exo-news-attachment :file="attachedFile"></exo-news-attachment>
                <div class="removeFile">
                  <a :title="$t('news.composer.attachments.delete')" href="#" class="actionIcon" rel="tooltip"
                     data-placement="top" @click="removeAttachedFile(attachedFile)">
                    <i class="uiIcon uiIconLightGray"></i>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
        <exo-server-files-selector v-if="showDocumentSelector" :attached-files="value" :space-id="spaceId" :max-files-count="maxFilesCount" @attachExistingServerAttachment="toggleServerFileSelector" @cancel="toggleServerFileSelector()"></exo-server-files-selector>
      </div>
      <div v-if="!showDocumentSelector" class="attachmentsFooter footer">
        <a class="btn" @click="closeAttachments()">{{ $t('news.composer.attachments.close') }}</a>
      </div>
    </div>
    <div v-show="showAttachments" class="drawer-backdrop" @click="closeAttachments()"></div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    },
    spaceId: {
      type: String,
      default: ''
    },
    maxFilesCount: {
      type: String,
      required: false,
      default: null
    },
    maxFileSize: {
      type: String,
      required: false,
      default: null
    }
  },
  data() {
    return {
      showAttachments: false,
      message: '',
      uploadingFilesQueue: [],
      uploadingCount : 0,
      maxUploadInProgressCount : 2,
      maxProgress : 100,
      showDocumentSelector: false,
      fileSizeLimitError: false,
      filesCountLimitError: false,
      BYTES_IN_MB: 1048576,
      MESSAGES_DISPLAY_TIME: 5000,
      drawerTitle: `${this.$t('news.composer.attachments.header')}`
    };
  },
  watch: {
    fileSizeLimitError: function() {
      if(this.fileSizeLimitError) {
        setTimeout(() => this.fileSizeLimitError = false, this.MESSAGES_DISPLAY_TIME);
      }
    },
    filesCountLimitError: function() {
      if(this.filesCountLimitError) {
        setTimeout(() => this.filesCountLimitError = false, this.MESSAGES_DISPLAY_TIME);
      }
    }
  },
  mounted() {
    ['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'drop'].forEach( function( evt ) {
      /*
        For each event add an event listener that prevents the default action
        (opening the file in the browser) and stop the propagation of the event (so
        no other elements open the file in the browser)
      */
      this.$refs.dropFileBox.addEventListener(evt, function(e) {
        e.preventDefault();
        e.stopPropagation();
      }.bind(this), false);
    }.bind(this));

    /*
      Capture the files from the drop event and add them to our local files
      array.
    */
    this.$refs.dropFileBox.addEventListener('drop', function(e) {
      this.handleFileUpload( e.dataTransfer.files );
    }.bind(this));
  },
  methods: {
    closeAttachments: function() {
      this.showAttachments = false;
    },
    setUploadingCount: function(uploadingCount) {
      this.uploading = uploadingCount > 0;
    },
    uploadFile: function() {
      this.$refs.uploadInput.click();
    },
    handleFileUpload: function(files) {
      const newFilesArray = Array.from(files);

      newFilesArray.sort(function(file1, file2) {
        return file1.size - file2.size;
      });

      const newAttachedFiles = [];
      newFilesArray.forEach(file => {
        newAttachedFiles.push({
          originalFileObject: file,
          name: file.name,
          size: file.size,
          mimetype: file.type,
          uploadId: this.getNewUploadId(),
          uploadProgress: 0
        });
      });

      newAttachedFiles.forEach(newFile => {
        this.queueUpload(newFile);
      });
    },
    getFormattedFileSize(fileSize) {
      const formattedSizePrecision = 2;
      const sizeMB = fileSize / this.BYTES_IN_MB;
      return sizeMB.toFixed(formattedSizePrecision);
    },
    getNewUploadId: function() {
      const maxUploadId = 100000;
      return Math.floor(Math.random() * maxUploadId);
    },
    queueUpload: function(file) {
      if(this.value.length >= this.maxFilesCount) {
        this.filesCountLimitError = true;
        return;
      }

      const fileSizeInMb = file.size / this.BYTES_IN_MB;
      if(fileSizeInMb > this.maxFileSize) {
        this.fileSizeLimitError = true;
        return;
      }

      this.value.push(file);
      if(this.uploadingCount < this.maxUploadInProgressCount) {
        this.sendFileToServer(file);
      } else {
        this.uploadingFilesQueue.push(file);
      }
    },
    processNextQueuedUpload: function() {
      if(this.uploadingFilesQueue.length > 0) {
        this.sendFileToServer(this.uploadingFilesQueue.shift());
      }
    },
    sendFileToServer : function(file) {
      this.uploadingCount++;
      this.$emit('uploadingCountChanged', this.uploadingCount);

      const formData = new FormData();
      formData.append('file', file.originalFileObject);

      const uploadUrl =`${eXo.env.server.context}/upload?action=upload&uploadId=${file.uploadId}`;
      // Had to use axios here since progress observation is still not supported by fetch
      axios.request({
        method: 'POST',
        url: uploadUrl,
        credentials: 'include',
        data: formData,
        onUploadProgress: (progress) => {
          file.uploadProgress = Math.round(progress.loaded * this.maxProgress / progress.total);
        }
      }).then(() => {

        // Check if the file has correctly been uploaded (progress=100) before refreshing the upload list
        const progressUrl = `${eXo.env.server.context}/upload?action=progress&uploadId=${file.uploadId}`;
        fetch(progressUrl)
          .then(response => response.text())
          .then(responseText => {
            // TODO fix malformed json from upload service
            let responseObject;
            try {
              // trick to parse malformed json
              eval(`responseObject = ${responseText}`); // eslint-disable-line no-eval
            } catch (err) {
              return;
            }

            if(!responseObject.upload[file.uploadId] || !responseObject.upload[file.uploadId].percent ||
                responseObject.upload[file.uploadId].percent !== this.maxProgress.toString()) {
              this.removeAttachedFile(file.uploadId);
            } else {
              file.uploadProgress = this.maxProgress;
            }
          });

        this.uploadingCount--;
        this.$emit('uploadingCountChanged', this.uploadingCount);
        this.processNextQueuedUpload();
      });
    },
    removeAttachedFile: function(file) {
      if(!file.id) {
        this.value = this.value.filter(attachedFile => attachedFile.uploadId !== file.uploadId);
        if(file.uploadProgress !== this.maxProgress) {
          this.uploadingCount--;
          this.$emit('uploadingCountChanged', this.uploadingCount);
          this.processNextQueuedUpload();
        }
      } else {
        this.value = this.value.filter(attachedFile => attachedFile.id !== file.id);
      }
      this.$emit('input', this.value);
    },
    toggleServerFileSelector(selectedFiles){
      if (selectedFiles) {
        this.value = selectedFiles;
        this.$emit('input', this.value);
      }
      this.showDocumentSelector = !this.showDocumentSelector;
      this.drawerTitle = this.showDocumentSelector? `${this.$t('news.composer.attachments.existingUploads')}` : `${this.$t('news.composer.attachments.header')}`;
    }
  }
};
</script>
/**
 * AngularJS Component for {@link domain.studies.ProcessingType ProcessingType} administration.
 *
 * @namespace admin.studies.components.processingTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

class ProcessingTypeViewController {

  constructor($scope,
              $q,
              $state,
              CollectionEventType,
              ProcessingType,
              gettextCatalog,
              modalService,
              modalInput,
              domainNotificationService,
              notificationsService,
              CollectionEventAnnotationTypeRemove,
              ProcessingTypeInputModal,
              ProcessingTypeOutputModal) {
    'ngInject';
    Object.assign(this,
                  {
                    $scope,
                    $q,
                    $state,
                    CollectionEventType,
                    ProcessingType,
                    gettextCatalog,
                    modalService,
                    modalInput,
                    domainNotificationService,
                    notificationsService,
                    CollectionEventAnnotationTypeRemove,
                    ProcessingTypeInputModal,
                    ProcessingTypeOutputModal
                  });
    this.annotationTypeRemove = new CollectionEventAnnotationTypeRemove();
  }

  $onInit() {
    this.isPanelCollapsed = false;
    this.allowChanges = this.study.isDisabled();

    const input = this.processingType.specimenProcessing.input;
    if (input.isCollected()) {
      this.getCollectionEventType(input.entityId);
    } else {
      this.getProcessingType(input.entityId);
    }
  }

  getCollectionEventType() {
    const input = this.processingType.specimenProcessing.input;
    this.CollectionEventType.getById(this.processingType.studyId, input.entityId)
      .then((reply) => {
        this.inputEntity = reply;
        this.inputSpecimenDefinition = _.find(reply.specimenDefinitions,
                                              { id: input.specimenDefinitionId });
      });
  }

  getProcessingType() {
    const input = this.processingType.specimenProcessing.input;
    this.ProcessingType.getById(this.study.id, input.entityId)
      .then((reply) => {
        this.inputEntity = reply;
        this.inputSpecimenDefinition = reply.specimenProcessing.output.specimenDefinition;
      });
  }

  postUpdate(message, title, timeout) {
    timeout = timeout || 1500;
    return (processingType) => {
      this.processingType = processingType;
      this.notificationsService.success(message, title, timeout);
    };
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Edit Event name'),
                         this.gettextCatalog.getString('Name'),
                         this.processingType.name,
                         { required: true, minLength: 2 }).result
      .then(name => {
        this.processingType.updateName(name)
          .then(processingType => {
            this.$scope.$emit('processing-type-updated', processingType);
            this.postUpdate(this.gettextCatalog.getString('Name changed successfully.'),
                            this.gettextCatalog.getString('Change successful'))(processingType);
            this.$state.go(this.$state.current.name,
                           {
                             studySlug:      this.study.slug,
                             processingTypeSlug: processingType.slug
                           },
                           { reload: true });
          })
          .catch(error => this.notificationsService.updateError(error));
      });
  }

  editDescription() {
    this.modalInput.textArea(this.gettextCatalog.getString('Edit Event description'),
                             this.gettextCatalog.getString('Description'),
                             this.processingType.description)
      .result
      .then(description => {
        this.processingType.updateDescription(description)
          .then(this.postUpdate(this.gettextCatalog.getString('Description changed successfully.'),
                                this.gettextCatalog.getString('Change successful')))
          .catch(error => this.notificationsService.updateError(error));
      });
  }

  editEnabled() {
    this.modalInput.boolean(this.gettextCatalog.getString('Edit Event enabled'),
                            this.gettextCatalog.getString('Enabled'),
                            this.processingType.enabled)
      .result
      .then(enabled => {
        this.processingType.updateEnabled(enabled)
          .then(processingType => {
            this.$scope.$emit('collection-event-type-updated', processingType);
            this.postUpdate(this.gettextCatalog.getString('Enabled changed successfully.'),
                            this.gettextCatalog.getString('Change successful'))(processingType);
          })
          .catch(error => this.notificationsService.updateError(error));
      });
  }

  addAnnotationType() {
    this.$state.go('home.admin.studies.study.processing.viewType.annotationTypeAdd');
  }

  editAnnotationType(annotType) {
    this.$state.go('home.admin.studies.study.processing.viewType.annotationTypeView',
                   { annotationTypeSlug: annotType.slug });
  }

  removeAnnotationType(annotationType) {
    if (_.includes(this.annotationTypeIdsInUse, annotationType.id)) {
      this.annotationTypeRemove.removeInUseModal(annotationType, this.annotationTypeName);
    } else {
      if (!this.study.isDisabled()) {
        throw new Error('modifications not allowed');
      }

      this.annotationTypeRemove.remove(
        annotationType,
        () => this.processingType.removeAnnotationType(annotationType)
      ).then(processingType => {
        this.processingType = processingType;
        this.notificationsService.success(this.gettextCatalog.getString('Annotation removed'));
      });
    }
  }

  inputSpecimenUpdate() {
    this.ProcessingTypeInputModal.open(this.study, _.cloneDeep(this.processingType))
      .result
      .then(inputSpecimenProcessing =>
            this.processingType.updateInputSpecimenProcessing(inputSpecimenProcessing))
      .then(processingType => {
        this.processingType = processingType;
        this.notificationsService.success(this.gettextCatalog.getString('Input specimen updated'));
      })
      .catch(error => {
        // if user pressed the 'Cancel' button then error is of type string and equal to 'cancel'
        //
        // otherwise, the server has replied with an error
        if (typeof error === 'object') {
          this.notificationsService.updateError(error);
        }
      });
  }

  outputSpecimenUpdate() {
    this.ProcessingTypeOutputModal.open(this.study, _.cloneDeep(this.processingType))
      .result
      .then(outputSpecimenProcessing =>
            this.processingType.updateOutputSpecimenProcessing(outputSpecimenProcessing))
      .then(processingType => {
        this.processingType = processingType;
        this.notificationsService.success(this.gettextCatalog.getString('Output specimen updated'));
      })
      .catch(error => {
        // if user pressed the 'Cancel' button then error is of type string and equal to 'cancel'
        //
        // otherwise, the server has replied with an error
        if (typeof error === 'object') {
          this.notificationsService.updateError(error);
        }
      });
  }

  removeProcessingType() {
    this.processingType.inUse().then(inUse => {
      if (inUse) {
        this.modalService.modalOk(
          this.gettextCatalog.getString('Event in use'),
          this.gettextCatalog.getString(
            'This event cannot be removed since one or more participants are using it. ' +
              'If you still want to remove it, the participants using it have to be modified ' +
              'to no longer use it.'));
      } else {
        const promiseFn = () => this.processingType.remove()
              .then(() => {
                this.notificationsService.success(
                  this.gettextCatalog.getString('Processing step removed'));
                this.$state.go('^', {}, { reload: true });
              });

        this.domainNotificationService.removeEntity(
          promiseFn,
          this.gettextCatalog.getString('Remove event'),
          this.gettextCatalog.getString(
            'Are you sure you want to remove processing step with name <strong>{{name}}</strong>?',
            { name: this.processingType.name }),
          this.gettextCatalog.getString('Remove failed'),
          this.gettextCatalog.getString('Processing step with name {{name}} cannot be removed',
                                        { name: this.processingType.name }));
      }
    });
  }
}

/**
 * An AngularJS component that displays a {@link domain.studies.ProcessingType ProcessingType} for a
 * {@link domain.studies.Study Study}.
 *
 * The component also allows the user to make changes to the processing type.
 *
 * @memberOf admin.studies.components.processingTypeView
 *
 * @param {domain.studies.Study} study - the study the processing type belongs to.
 *
 * @param {domain.studies.ProcessingType} processingType - the processing types to display.
 */
const processingTypeViewComponent = {
  template: require('./processingTypeView.html'),
  controller: ProcessingTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:          '<',
    processingType: '<'
  }
};

function resolveProcessingType($transition$, study, ProcessingType, resourceErrorService) {
  'ngInject';
  const slug = $transition$.params().processingTypeSlug
  return ProcessingType.get(study.slug, slug)
    .catch(resourceErrorService.goto404(
      `processing type ID not found: studyId/${study.slug}, processingTypeSlug/${slug}`))
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing.viewType', {
    url: '/step/{processingTypeSlug}',
    resolve: {
      processingType: resolveProcessingType
    },
    views: {
      'processingTypeDetails': 'processingTypeView'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeView', processingTypeViewComponent);
}

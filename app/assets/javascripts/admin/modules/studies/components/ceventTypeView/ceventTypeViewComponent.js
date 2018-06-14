/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.ceventTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

class CeventTypeViewController {

  constructor($scope,
              $state,
              gettextCatalog,
              modalService,
              modalInput,
              domainNotificationService,
              notificationsService,
              CollectionEventAnnotationTypeRemove) {
    'ngInject';
    Object.assign(this,
                  {
                    $scope,
                    $state,
                    gettextCatalog,
                    modalService,
                    modalInput,
                    domainNotificationService,
                    notificationsService,
                    CollectionEventAnnotationTypeRemove
                  });
    this.annotationTypeRemove = new CollectionEventAnnotationTypeRemove();
  }

  $onInit() {
    this.isPanelCollapsed = false;
    this.allowChanges = this.study.isDisabled();
    this.hasSpecimenDefinitions = (this.collectionEventType.specimenDefinitions.length > 0);
  }

  postUpdate(message, title, timeout) {
    timeout = timeout || 1500;
    return (ceventType) => {
      this.collectionEventType = ceventType;
      this.notificationsService.success(message, title, timeout);
    };
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Edit Event name'),
                         this.gettextCatalog.getString('Name'),
                         this.collectionEventType.name,
                         { required: true, minLength: 2 }).result
      .then(name => {
        this.collectionEventType.updateName(name)
          .then(ceventType => {
            this.$scope.$emit('collection-event-type-updated', ceventType);
            this.postUpdate(this.gettextCatalog.getString('Name changed successfully.'),
                            this.gettextCatalog.getString('Change successful'))(ceventType);
            this.$state.go(this.$state.current.name,
                           {
                             studySlug:      this.study.slug,
                             ceventTypeSlug: ceventType.slug
                           },
                           { reload: true });
          })
          .catch(this.notificationsService.updateError);
      });
  }

  editDescription() {
    this.modalInput.textArea(this.gettextCatalog.getString('Edit Event description'),
                             this.gettextCatalog.getString('Description'),
                             this.collectionEventType.description)
      .result
      .then(description => {
        this.collectionEventType.updateDescription(description)
          .then(this.postUpdate(this.gettextCatalog.getString('Description changed successfully.'),
                                this.gettextCatalog.getString('Change successful')))
          .catch(this.notificationsService.updateError);
      });
  }

  editRecurring() {
    this.modalInput.boolean(this.gettextCatalog.getString('Edit Event recurring'),
                            this.gettextCatalog.getString('Recurring'),
                            this.collectionEventType.recurring)
      .result
      .then(recurring => {
        this.collectionEventType.updateRecurring(recurring)
          .then(ceventType => {
            this.$scope.$emit('collection-event-type-updated', ceventType);
            this.postUpdate(this.gettextCatalog.getString('Recurring changed successfully.'),
                            this.gettextCatalog.getString('Change successful'))(ceventType);
          })
          .catch(this.notificationsService.updateError);
      });
  }

  addAnnotationType() {
    this.$state.go('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
  }

  editAnnotationType(annotType) {
    this.$state.go('home.admin.studies.study.collection.ceventType.annotationTypeView',
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
        () => this.collectionEventType.removeAnnotationType(annotationType)
      ).then(collectionEventType => {
        this.collectionEventType = collectionEventType;
        this.notificationsService.success(this.gettextCatalog.getString('Annotation removed'));
      });
    }
  }

  addSpecimenDefinition() {
    this.$state.go('home.admin.studies.study.collection.ceventType.specimenDefinitionAdd');
  }

  editSpecimenDefinition(specimenDefinition) {
    this.$state.go('home.admin.studies.study.collection.ceventType.specimenDefinitionView',
                   { specimenDefinitionSlug: specimenDefinition.slug });
  }

  removeSpecimenDefinition(specimenDefinition) {
    if (!this.study.isDisabled()) {
      throw new Error('modifications not allowed');
    }

    const removePromiseFunc = () => this.collectionEventType.removeSpecimenDefinition(specimenDefinition)
          .then(collectionEventType => {
            this.collectionEventType = collectionEventType;
            this.notificationsService.success(this.gettextCatalog.getString('Specimen removed'));
            this.$state.reload();
          });

    return this.domainNotificationService.removeEntity(
      removePromiseFunc,
      this.gettextCatalog.getString('Remove specimen'),
      this.gettextCatalog.getString('Are you sure you want to remove specimen {{name}}?',
                                    { name: specimenDefinition.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('Specimen {{name} cannot be removed',
                                    { name: specimenDefinition.name }));
  }

  panelButtonClicked() {
    this.isPanelCollapsed = !this.isPanelCollapsed;
  }

  removeCeventType() {
    this.collectionEventType.inUse().then(inUse => {
      if (inUse) {
        this.modalService.modalOk(
          this.gettextCatalog.getString('Event in use'),
          this.gettextCatalog.getString(
            'This event cannot be removed since one or more participants are using it. ' +
              'If you still want to remove it, the participants using it have to be modified ' +
              'to no longer use it.'));
      } else {
        const promiseFn = () => this.collectionEventType.remove()
              .then(() => {
                this.notificationsService.success(
                  this.gettextCatalog.getString('Event removed'));
                this.$state.go('^', {}, { reload: true });
              });

        this.domainNotificationService.removeEntity(
          promiseFn,
          this.gettextCatalog.getString('Remove event'),
          this.gettextCatalog.getString(
            'Are you sure you want to remove event with name <strong>{{name}}</strong>?',
            { name: this.collectionEventType.name }),
          this.gettextCatalog.getString('Remove failed'),
          this.gettextCatalog.getString('Event with name {{name}} cannot be removed',
                                        { name: this.collectionEventType.name }));
      }
    });
  }
}

/**
 * An AngularJS component that displays a {@link domain.studies.CollectionEventType CollectionEventType} for a
 * {@link domain.studies.Study Study}.
 *
 * The component also allows the user to make changes to the collection event type.
 *
 * @memberOf admin.studies.components.ceventTypeView
 *
 * @param {domain.studies.Study} study - the study the collection event type belongs to.
 *
 * @param {domain.studies.CollectionEventType} collectionEventType - the collection event types to display.
 */
const ceventTypeViewComponent = {
  template: require('./ceventTypeView.html'),
  controller: CeventTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<'
  }
};

function resolveCollectionEventType($transition$, study, CollectionEventType, resourceErrorService) {
  'ngInject';
  const slug = $transition$.params().ceventTypeSlug
  return CollectionEventType.get(study.slug, slug)
    .catch(resourceErrorService.goto404(
      `collection event type ID not found: studyId/${study.slug}, ceventTypeSlug/${slug}`))
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.collection.ceventType', {
    url: '/events/{ceventTypeSlug}',
    resolve: {
      collectionEventType: resolveCollectionEventType
    },
    views: {
      'ceventTypeDetails': 'ceventTypeView'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('ceventTypeView', ceventTypeViewComponent);
}

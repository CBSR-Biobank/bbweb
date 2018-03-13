/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.ceventTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */


import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function CeventTypeViewController($scope,
                                  $state,
                                  gettextCatalog,
                                  modalService,
                                  modalInput,
                                  domainNotificationService,
                                  notificationsService,
                                  CollectionEventAnnotationTypeRemove) {
  var vm = this;
  vm.$onInit = onInit;
  vm.annotationTypeRemove = new CollectionEventAnnotationTypeRemove();

  //--

  function onInit() {
    vm.isPanelCollapsed     = false;

    vm.editName                  = editName;
    vm.editDescription           = editDescription;
    vm.editRecurring             = editRecurring;
    vm.editSpecimenDescription   = editSpecimenDescription;
    vm.editAnnotationType        = editAnnotationType;
    vm.removeAnnotationType      = removeAnnotationType;
    vm.addAnnotationType         = addAnnotationType;
    vm.removeSpecimenDescription = removeSpecimenDescription;
    vm.addSpecimenDescription    = addSpecimenDescription;
    vm.addSpecimenDescription    = addSpecimenDescription;
    vm.panelButtonClicked        = panelButtonClicked;
    vm.removeCeventType          = removeCeventType;
  }

  function postUpdate(message, title, timeout) {
    timeout = timeout || 1500;
    return function (ceventType) {
      vm.collectionEventType = ceventType;
      notificationsService.success(message, title, timeout);
    };
  }

  function editName() {
    modalInput.text(gettextCatalog.getString('Edit Event Type name'),
                    gettextCatalog.getString('Name'),
                    vm.collectionEventType.name,
                    { required: true, minLength: 2 }).result
      .then(function (name) {
        vm.collectionEventType.updateName(name)
          .then(ceventType => {
            $scope.$emit('collection-event-type-updated', ceventType);
            postUpdate(gettextCatalog.getString('Name changed successfully.'),
                       gettextCatalog.getString('Change successful'))(ceventType);
            $state.go($state.current.name,
                      {
                        studySlug:      vm.study.slug,
                        ceventTypeSlug: ceventType.slug
                      },
                      { reload: true });
          })
          .catch(notificationsService.updateError);
      });
  }

  function editDescription() {
    modalInput.textArea(gettextCatalog.getString('Edit Event Type description'),
                        gettextCatalog.getString('Description'),
                        vm.collectionEventType.description
                       ).result
      .then(function (description) {
        vm.collectionEventType.updateDescription(description)
          .then(postUpdate(gettextCatalog.getString('Description changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

  function editRecurring() {
    modalInput.boolean(gettextCatalog.getString('Edit Event Type recurring'),
                       gettextCatalog.getString('Recurring'),
                       vm.collectionEventType.recurring.toString()
                      ).result
      .then(function (recurring) {
        vm.collectionEventType.updateRecurring(recurring === 'true')
          .then(function (ceventType) {
            $scope.$emit('collection-event-type-updated', ceventType);
            postUpdate(gettextCatalog.getString('Recurring changed successfully.'),
                       gettextCatalog.getString('Change successful'))(ceventType);
          })
          .catch(notificationsService.updateError);
      });
  }

  function addAnnotationType() {
    $state.go('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
  }

  function addSpecimenDescription() {
    $state.go('home.admin.studies.study.collection.ceventType.specimenDescriptionAdd');
  }

  function editAnnotationType(annotType) {
    $state.go('home.admin.studies.study.collection.ceventType.annotationTypeView',
              { annotationTypeSlug: annotType.slug });
  }

  function removeAnnotationType(annotationType) {
    if (_.includes(vm.annotationTypeIdsInUse, annotationType.id)) {
      vm.annotationTypeRemove.removeInUseModal(annotationType, vm.annotationTypeName);
    } else {
      if (!vm.study.isDisabled()) {
        throw new Error('modifications not allowed');
      }

      vm.annotationTypeRemove.remove(
        annotationType,
        () => vm.collectionEventType.removeAnnotationType(annotationType)
          .then(collectionEventType => {
            vm.collectionEventType = collectionEventType;
            notificationsService.success(gettextCatalog.getString('Annotation removed'));
          }));
    }
  }

  function editSpecimenDescription(specimenDescription) {
    $state.go('home.admin.studies.study.collection.ceventType.specimenDescriptionView',
              { specimenDescriptionSlug: specimenDescription.slug });
  }

  function removeSpecimenDescription(specimenDescription) {
    if (!vm.study.isDisabled()) {
      throw new Error('modifications not allowed');
    }

    return domainNotificationService.removeEntity(
      removePromiseFunc,
      gettextCatalog.getString('Remove specimen'),
      gettextCatalog.getString('Are you sure you want to remove specimen {{name}}?',
                               { name: specimenDescription.name }),
      gettextCatalog.getString('Remove failed'),
      gettextCatalog.getString('Specimen {{name} cannot be removed',
                               { name: specimenDescription.name }));

    function removePromiseFunc() {
      return vm.collectionEventType.removeSpecimenDescription(specimenDescription)
        .then(function (collectionEventType) {
          vm.collectionEventType = collectionEventType;
          notificationsService.success(gettextCatalog.getString('Specimen removed'));
          $state.reload();
        });
    }
  }

  function panelButtonClicked() {
    vm.isPanelCollapsed = !vm.isPanelCollapsed;
  }

  function removeCeventType() {
    vm.collectionEventType.inUse().then(function (inUse) {
      if (inUse) {
        modalService.modalOk(
          gettextCatalog.getString('Collection event in use'),
          gettextCatalog.getString(
            'This collection event cannot be removed since one or more participants are using it. ' +
              'If you still want to remove it, the participants using it have to be modified ' +
              'to no longer use it.'));
      } else {
        domainNotificationService.removeEntity(
          promiseFn,
          gettextCatalog.getString('Remove collection event'),
          gettextCatalog.getString(
            'Are you sure you want to remove collection event with name <strong>{{name}}</strong>?',
            { name: vm.collectionEventType.name }),
          gettextCatalog.getString('Remove failed'),
          gettextCatalog.getString('Collection event with name {{name}} cannot be removed',
                                   { name: vm.collectionEventType.name }));
      }
    });

    function promiseFn() {
      return vm.collectionEventType.remove().then(function () {
        notificationsService.success(gettextCatalog.getString('Collection event removed'));
        $state.go('home.admin.studies.study.collection', {}, { reload: true });
      });
    }
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

export default ngModule => ngModule.component('ceventTypeView', ceventTypeViewComponent)

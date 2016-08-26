/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function ceventTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:      '=',
        ceventType: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/ceventTypeView/ceventTypeView.html',
      controller: CeventTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypeViewCtrl.$inject = [
    '$state',
    'gettext',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'domainNotificationService',
    'notificationsService',
    'CollectionEventAnnotationTypeModals'
  ];

  function CeventTypeViewCtrl($state,
                              gettext,
                              gettextCatalog,
                              modalService,
                              modalInput,
                              domainNotificationService,
                              notificationsService,
                              CollectionEventAnnotationTypeModals) {
    var vm = this;

    _.extend(vm, new CollectionEventAnnotationTypeModals());

    vm.isPanelCollapsed     = false;

    vm.editName             = editName;
    vm.editDescription      = editDescription;
    vm.editRecurring        = editRecurring;
    vm.editSpecimenSpec     = editSpecimenSpec;
    vm.editAnnotationType   = editAnnotationType;
    vm.removeAnnotationType = removeAnnotationType;
    vm.addAnnotationType    = addAnnotationType;
    vm.removeSpecimenSpec   = removeSpecimenSpec;
    vm.addSpecimenSpec      = addSpecimenSpec;
    vm.addSpecimenSpec      = addSpecimenSpec;
    vm.panelButtonClicked   = panelButtonClicked;
    vm.removeCeventType     = removeCeventType;

    //--

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (ceventType) {
        vm.ceventType = ceventType;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(gettext('Edit Event Type name'),
                      gettext('Name'),
                      vm.ceventType.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.ceventType.updateName(name)
            .then(postUpdate(gettext('Name changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea(gettext('Edit Event Type description'),
                          gettext('Description'),
                          vm.ceventType.description
                         ).result
        .then(function (description) {
          vm.ceventType.updateDescription(description)
            .then(postUpdate(gettext('Description changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
      });
    }

    function editRecurring() {
      modalInput.boolean(gettext('Edit Event Type recurring'),
                         gettext('Recurring'),
                         vm.ceventType.recurring.toString()
                        ).result
        .then(function (recurring) {
          vm.ceventType.updateRecurring(recurring === 'true')
            .then(postUpdate(gettext('Recurring changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
      });
    }

    function addAnnotationType() {
      $state.go('home.admin.studies.study.collection.ceventType.annotationTypeAdd');
    }

    function addSpecimenSpec() {
      $state.go('home.admin.studies.study.collection.ceventType.specimenSpecAdd');
    }

    function editSpecimenSpec(specimenSpec) {
      $state.go('home.admin.studies.study.collection.ceventType.specimenSpecView',
                { specimenSpecId: specimenSpec.uniqueId });
    }

    function removeSpecimenSpec(specimenSpec) {
      if (!vm.study.isDisabled()) {
        throw new Error('modifications not allowed');
      }

      return domainNotificationService.removeEntity(
        removePromiseFunc,
        gettext('Remove specimen'),
        gettextCatalog.getString('Are you sure you want to remove specimen {{name}}?',
                                 { name: specimenSpec.name }),
        gettext('Remove failed'),
        gettextCatalog.getString('Specimen {[name]} cannot be removed',
                                 { name: specimenSpec.name }));

      function removePromiseFunc() {
        return vm.ceventType.removeSpecimenSpec(specimenSpec);
      }
    }

    function editAnnotationType(annotType) {
      $state.go('home.admin.studies.study.collection.ceventType.annotationTypeView',
                { annotationTypeId: annotType.uniqueId });
    }

    function removeAnnotationType(annotationType) {
      if (_.includes(vm.annotationTypeIdsInUse, annotationType.uniqueId)) {
        vm.removeInUseModal(annotationType, vm.annotationTypeName);
      } else {
        if (!vm.study.isDisabled()) {
          throw new Error('modifications not allowed');
        }

        vm.remove(annotationType, function () {
          return vm.ceventType.removeAnnotationType(annotationType);
        });
      }
    }

    function panelButtonClicked() {
      vm.isPanelCollapsed = !vm.isPanelCollapsed;
    }

    function removeCeventType() {
      vm.ceventType.inUse().then(function (inUse) {
        if (inUse) {
          modalService.modalOk(
            gettext('Collection event in use'),
            gettext('This collection event cannot be removed since one or more participants are using it. ' +
                    'If you still want to remove it, the participants using it have to be modified ' +
                    'to no longer use it.'));
        } else {
          domainNotificationService.removeEntity(
            promiseFn,
            gettext('Remove collection event'),
            gettext('Are you sure you want to remove collection event with name <strong>{{name}}</strong>?',
                    { name: vm.ceventType.name }),
            gettext('Remove failed'),
            gettext('Collection event with name {{name}} cannot be removed',
                    { name: vm.ceventType.name }));
        }

        function promiseFn() {
          return vm.ceventType.remove().then(function () {
            notificationsService.success(gettext('Collection event removed'));
            $state.go('home.admin.studies.study.collection', {}, { reload: true });
          });
        }
      });
    }
  }

  return ceventTypeViewDirective;

});

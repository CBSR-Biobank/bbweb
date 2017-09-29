/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    template: require('./ceventView.html'),
    controller: CeventViewController,
    controllerAs: 'vm',
    bindings: {
      study:                '<',
      collectionEvent:      '<',
      collectionEventTypes: '<'
    }
  };

  CeventViewController.$inject = [
    '$scope',
    '$state',
    'gettextCatalog',
    'CollectionEvent',
    'Specimen',
    'timeService',
    'modalService',
    'modalInput',
    'domainNotificationService',
    'notificationsService',
    'annotationUpdate'
  ];

  /*
   * Controller for this component.
   */
  function CeventViewController($scope,
                                $state,
                                gettextCatalog,
                                CollectionEvent,
                                Specimen,
                                timeService,
                                modalService,
                                modalInput,
                                domainNotificationService,
                                notificationsService,
                                annotationUpdate) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.collectionEventType = _.find(vm.collectionEventTypes, { id: vm.collectionEvent.collectionEventTypeId });

      vm.panelOpen                      = true;
      vm.editTimeCompleted              = editTimeCompleted;
      vm.editAnnotation                 = editAnnotation;
      vm.panelButtonClicked             = panelButtonClicked;
      vm.remove                         = remove;
      vm.getAnnotationUpdateButtonTitle = getAnnotationUpdateButtonTitle;
    }

    function postUpdate(message, title, timeout) {
      return function (cevent) {
        vm.collectionEvent = cevent;
        vm.collectionEvent.setCollectionEventType(vm.collectionEventType);
        notificationsService.success(message, title, timeout);
      };
    }

    function editTimeCompleted() {
      modalInput.dateTime(gettextCatalog.getString('Update time completed'),
                          gettextCatalog.getString('Time completed'),
                          vm.collectionEvent.timeCompleted,
                          { required: true })
        .result.then(function (timeCompleted) {
          vm.collectionEvent.updateTimeCompleted(timeService.dateAndTimeToUtcString(timeCompleted))
            .then(function (cevent) {
              $scope.$emit('collection-event-updated', cevent);
              postUpdate(gettextCatalog.getString('Time completed updated successfully.'),
                         gettextCatalog.getString('Change successful'),
                         1500)(cevent);
            })
            .catch(notificationsService.updateError);
        });
    }

    function editAnnotation(annotation) {
      annotationUpdate.update(annotation, 'Update ' + annotation.getLabel())
        .then(function (newAnnotation) {
          vm.collectionEvent.addAnnotation(newAnnotation)
            .then(postUpdate(gettextCatalog.getString('Annotation updated successfully.'),
                             gettextCatalog.getString('Change successful'),
                             1500))
            .catch(notificationsService.updateError);
        });
    }

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }

    function remove() {
      Specimen.list(vm.collectionEvent.id).then(function (pagedResult) {
        if (pagedResult.items.length > 0) {
          modalService.modalOk(
            gettextCatalog.getString('Cannot remove collection event'),
            gettextCatalog.getString('This collection event has specimens. Please remove the specimens first.'));
        } else {
          domainNotificationService.removeEntity(
            promiseFn,
            gettextCatalog.getString('Remove event'),
            /// visit number comes from the collection event
            gettextCatalog.getString(
              'Are you sure you want to remove event with visit # <strong>{{visitNumber}}</strong>?',
              { visitNumber: vm.collectionEvent.visitNumber}),
            gettextCatalog.getString('Remove failed'),
            gettextCatalog.getString(
              'Collection event with visit number {{visitNumber}} cannot be removed',
              { visitNumber: vm.collectionEvent.visitNumber}));
        }

        function promiseFn() {
          return vm.collectionEvent.remove().then(function () {
            notificationsService.success(gettextCatalog.getString('Collection event removed'));
            $state.go('home.collection.study.participant.cevents', {}, { reload: true });
          });
        }
      });
    }

    function getAnnotationUpdateButtonTitle(annotation) {
      /// label is a name assigned by the user for an annotation type
      return gettextCatalog.getString('Update {{label}}', { label: annotation.getLabel() });
    }

  }

  return component;
});

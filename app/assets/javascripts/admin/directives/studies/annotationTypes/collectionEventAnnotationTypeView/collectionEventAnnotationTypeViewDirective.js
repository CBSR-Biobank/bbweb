/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionEventAnnotationTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        collectionEventType: '=',
        annotationType:      '='
      },
      template: [
        '<annotation-type-view',
        '  study="vm.study"',
        '  annotation-type="vm.annotationType"',
        '  return-state="home.admin.studies.study.collection.view"',
        '  on-update="vm.onUpdate">',
        '</annotation-type-view>'
      ].join(''),
      controller: CollectionEventAnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionEventAnnotationTypeViewCtrl.$inject = [
    '$q',
    'notificationsService'
  ];

  function CollectionEventAnnotationTypeViewCtrl($q, notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      return vm.collectionEventType.updateAnnotationType(annotationType)
        .then(postUpdate)
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function postUpdate(collectionEventType) {
      var deferred = $q.defer();
      vm.collectionEventType = collectionEventType;
      vm.annotationType = _.findWhere(vm.collectionEventType.annotationTypes,
                                      { uniqueId: vm.annotationType.uniqueId });
      if (_.isUndefined(vm.annotationType)) {
        deferred.reject('could not update annotation type');
      } else {
        deferred.resolve(true);
      }
      return deferred.promise;
    }

    function notifySuccess() {
      return notificationsService.success(
        'Annotation type changed successfully.',
        'Change successful',
        1500);
    }

  }

  return collectionEventAnnotationTypeViewDirective;

});

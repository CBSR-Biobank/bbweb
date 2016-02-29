/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CeventTypeEditCtrl.$inject = [
    '$state',
    'CollectionEventType',
    'domainEntityService',
    'notificationsService',
    'study'
  ];

  /**
   * Used to add or update a collection event type.
   */
  function CeventTypeEditCtrl($state,
                              CollectionEventType,
                              domainEntityService,
                              notificationsService,
                              study) {
    var vm = this;

    vm.ceventType            = new CollectionEventType();
    vm.ceventType.studyId = study.id;

    vm.title                 = 'Add Collection Event Type';
    vm.submit                = submit;
    vm.cancel                = cancel;

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.collection', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(ceventType) {
      ceventType.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'collection event type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

  return CeventTypeEditCtrl;
});

/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Directive: ceventTypeAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.Study               = self.$injector.get('Study');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.factory        = self.$injector.get('factory');

      self.study = new self.Study(self.factory.study());
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/ceventTypeAdd/ceventTypeAdd.html');

      //--

      function createController(study) {
        self.element = angular.element([
          '<cevent-type-add',
          '  study="vm.study">',
          '<cevent-type-add>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = { study: study };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('ceventTypeAdd');
      }

    }));

    it('has valid scope when created', function () {
      this.createController(this.study);
      expect(this.controller.ceventType.isNew()).toBe(true);
    });

    it('can submit a collection event type', function() {
      var $q                   = this.$injector.get('$q'),
          notificationsService = this.$injector.get('notificationsService'),
          $state               = this.$injector.get('$state'),
          ceventType;

      ceventType = new this.CollectionEventType(this.factory.collectionEventType(this.study));
      this.createController(this.study);

      spyOn(this.CollectionEventType.prototype, 'add').and.callFake(function () {
        return $q.when();
      });
      spyOn(notificationsService, 'submitSuccess').and.callFake(function () {});
      spyOn($state, 'go').and.callFake(function () {});

      this.controller.submit(ceventType);
      this.scope.$digest();

      expect(notificationsService.submitSuccess).toHaveBeenCalled();
      expect($state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection', {}, { reload: true });
    });

    it('on submit error, displays an error modal', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          ceventType;

      ceventType = new this.CollectionEventType(this.factory.collectionEventType(this.study));
      this.createController(this.study);

      spyOn(this.CollectionEventType.prototype, 'add').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('simulated error for test');
        return deferred.promise;
      });
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});

      this.controller.submit(ceventType);
      this.scope.$digest();

      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('simulated error for test', 'collection event type');
    });

    it('when user presses the cancel button, goes to correct state', function() {
      var state = this.$injector.get('$state');

      spyOn(state, 'go').and.callFake(function () {});
      this.createController(this.study);

      this.controller.cancel();
      this.scope.$digest();
      expect(state.go).toHaveBeenCalledWith('home.admin.studies.study.collection');
    });

  });

});

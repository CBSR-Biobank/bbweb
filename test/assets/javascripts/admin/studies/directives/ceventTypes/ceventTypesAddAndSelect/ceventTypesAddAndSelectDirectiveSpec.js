/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventTypesAddAndSelectDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'CollectionEventType',
                              'factory');

      jsonStudy = self.factory.study();
      jsonCet   = self.factory.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));
      spyOn(this.$state, 'go').and.callFake(function () {});

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');

      this.createController = function (collectionEventTypes) {
        collectionEventTypes = collectionEventTypes || self.collectionEventTypes;

        self.element = angular.element(
          '<cevent-types-add-and-select collection-event-types="vm.collectionEventTypes">' +
            '</cevent-types-add-and-select>');
        self.scope = self.$rootScope.$new();
        self.scope.vm = { study: collectionEventTypes };

        self.$compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('ceventTypesAddAndSelect');
      };
    }));

    it('has valid scope', function() {
      this.createController();
      expect(this.controller.collectionEventTypes).toBe(this.collectionEventTypes);
    });

    it('function add switches to correct state', function() {
      this.createController();
      this.controller.add();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('function select switches to correct state', function() {
      this.createController();
      this.controller.select(this.collectionEventType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType',
                                                  { ceventTypeId: this.collectionEventType.id });
    });

    it('function recurring returns a valid result', function() {
      this.createController();
      this.collectionEventType.recurring = false;
      expect(this.controller.getRecurringLabel(this.collectionEventType)).toBe('Not recurring');
      this.collectionEventType.recurring = true;
      expect(this.controller.getRecurringLabel(this.collectionEventType)).toBe('Recurring');
    });

  });

});

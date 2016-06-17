/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('studyCollectionDirective', function() {

    var createDirective = function () {
      this.element = angular.element([
        '<study-collection',
        '   study="study">',
        '</study-collection>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.study = this.study;

      this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
      this.scope.$on('study-view', this.eventRxFunc);

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'CollectionEventType',
                              'factory');

      jsonStudy = self.factory.study();
      jsonCet   = self.factory.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/studyCollection/studyCollection.html',
        '/assets/javascripts/admin/studies/directives/studyNotDisabledWarning/studyNotDisabledWarning.html',
        '/assets/javascripts/admin/studies/directives/collection/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');
    }));

    it('initialization is valid', function() {
      createDirective.call(this);
      expect(this.scope.study).toBe(this.study);
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

  });

});

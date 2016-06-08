/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('StudyCounts', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(entityTestSuite,
                               extendedDomainEntities,
                               serverReplyMixin) {
      var self = this;

      _.extend(self, entityTestSuite, serverReplyMixin);

      self.StudyCounts = self.$injector.get('StudyCounts');
      self.httpBackend = self.$injector.get('$httpBackend');
    }));

    afterEach(function() {
      this.httpBackend.verifyNoOutstandingExpectation();
      this.httpBackend.verifyNoOutstandingRequest();
    });

    function fakeStudyCounts() {
      var disabled = faker.random.number(),
          enabled = faker.random.number(),
          retired = faker.random.number();
      return {
        total:    disabled + enabled + retired,
        disabledCount: disabled,
        enabledCount:  enabled,
        retiredCount:  retired
      };
    }

    it('can get created from empty object', function() {
      var counts = new this.StudyCounts();
      expect(counts.total).toEqual(0);
      expect(counts.disabled).toEqual(0);
      expect(counts.enabled).toEqual(0);
      expect(counts.retired).toEqual(0);
    });

    it('can get study counts from server', function() {
      var counts = fakeStudyCounts();
      this.httpBackend.whenGET('/studies/counts').respond(this.reply(counts));
      this.StudyCounts.get().then(expectCounts).catch(failTest);
      this.httpBackend.flush();

      function expectCounts(replyCounts) {
        expect(replyCounts.total).toEqual(counts.total);
        expect(replyCounts.disabled).toEqual(counts.disabledCount);
        expect(replyCounts.enabled).toEqual(counts.enabledCount);
        expect(replyCounts.retired).toEqual(counts.retiredCount);
      }

      function failTest(error) {
        expect(error).toBeUndefined();
      }
    });


  });

});

/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import faker from 'faker';

describe('UserCounts', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite, ServerReplyMixin) {
      _.extend(this, EntityTestSuite.prototype, ServerReplyMixin.prototype);
      this.injectDependencies('UserCounts', '$httpBackend');
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  function fakeUserCounts() {
    var registered = faker.random.number(),
        active = faker.random.number(),
        locked = faker.random.number();
    return {
      total:    registered + active + locked,
      registeredCount: registered,
      activeCount:  active,
      lockedCount:  locked
    };
  }

  it('can get created from empty object', function() {
    var counts = new this.UserCounts();
    expect(counts.total).toEqual(0);
    expect(counts.registered).toEqual(0);
    expect(counts.active).toEqual(0);
    expect(counts.locked).toEqual(0);
  });

  it('can get user counts from server', function() {
    var counts = fakeUserCounts();
    this.$httpBackend.whenGET('/api/users/counts').respond(this.reply(counts));
    this.UserCounts.get().then(expectCounts).catch(failTest);
    this.$httpBackend.flush();

    function expectCounts(replyCounts) {
      expect(replyCounts.total).toEqual(counts.total);
      expect(replyCounts.registered).toEqual(counts.registeredCount);
      expect(replyCounts.active).toEqual(counts.activeCount);
      expect(replyCounts.locked).toEqual(counts.lockedCount);
    }

    function failTest(error) {
      expect(error).toBeUndefined();
    }
  });

});
